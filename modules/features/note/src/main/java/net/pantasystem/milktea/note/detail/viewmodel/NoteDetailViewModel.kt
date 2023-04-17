package net.pantasystem.milktea.note.detail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.filter.WordFilterService
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache

class NoteDetailViewModel @AssistedInject constructor(
    accountRepository: AccountRepository,
    private val noteCaptureAdapter: NoteCaptureAPIAdapter,
    private val noteRelationGetter: NoteRelationGetter,
    private val noteRepository: NoteRepository,
    private val noteTranslationStore: NoteTranslationStore,
    val metaRepository: MetaRepository,
    private val noteDataSource: NoteDataSource,
    private val configRepository: LocalConfigRepository,
    private val emojiRepository: CustomEmojiRepository,
    private val noteWordFilterService: WordFilterService,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    private val loggerFactory: Logger.Factory,
    @Assisted val show: Pageable.Show,
    @Assisted val accountId: Long? = null,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(show: Pageable.Show, accountId: Long?): NoteDetailViewModel
    }

    private val logger by lazy {
        loggerFactory.create("NoteDetailVM")
    }

    companion object;

    private val currentAccountWatcher: CurrentAccountWatcher =
        CurrentAccountWatcher(accountId, accountRepository)

    private val cache =
        planeNoteViewDataCacheFactory.create(currentAccountWatcher::getAccount, viewModelScope)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val note = suspend {
        currentAccountWatcher.getAccount()
    }.asFlow().flatMapLatest {
        noteDataSource.observeOne(Note.Id(it.accountId, show.noteId))
    }.onStart {
        emit(null)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val threadContext = note.filterNotNull().flatMapLatest {
        noteRepository.observeThreadContext(it.id)
    }.catch {
        logger.error("ThreadContextの取得に失敗", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteThreadContext(emptyList(), emptyList()))

    val notes = combine(note, threadContext) { note, thread ->
        val relatedConversation = noteRelationGetter.getIn(thread.descendants.map { it.id }).filterNot {
            noteWordFilterService.isShouldFilterNote(show, it)
        }.map {
            NoteType.Conversation(it)
        }
        val repliesMap = thread.ancestors.groupBy {
            it.replyId
        }
        val relatedChildren = noteRelationGetter.getIn((repliesMap[note?.id] ?: emptyList()).map {
            it.id
        }).filterNot {
            noteWordFilterService.isShouldFilterNote(show, it)
        }.map { childNote ->
            NoteType.Children(childNote,
                noteRelationGetter.getIn(repliesMap[childNote.note.id]?.map { it.id }
                    ?: emptyList())
            )
        }
        val relatedNote =
            noteRelationGetter.getIn(if (note == null) emptyList() else listOf(note.id)).filterNot {
                noteWordFilterService.isShouldFilterNote(show, it)
            }.map {
                NoteType.Detail(it)
            }
        relatedConversation + relatedNote + relatedChildren
    }.map { notes ->
        notes.map { note ->
            when (note) {
                is NoteType.Children -> {
                    NoteConversationViewData(
                        note.note,
                        currentAccountWatcher.getAccount(),
                        noteTranslationStore,
                        viewModelScope,
                        noteDataSource,
                        emojiRepository,
                        configRepository,
                    ).also {
                        it.capture()
                        cache.put(it)
                    }.apply {
                        this.hasConversation.postValue(false)
                        this.conversation.postValue(note.getReplies().map {
                            cache.get(it)
                        })
                    }
                }
                is NoteType.Conversation -> {
                    cache.get(note.note)
                }
                is NoteType.Detail -> {
                    NoteDetailViewData(
                        note.note,
                        currentAccountWatcher.getAccount(),
                        noteTranslationStore,
                        noteDataSource,
                        configRepository,
                        emojiRepository,
                        viewModelScope,
                    ).also {
                        it.capture()
                        cache.put(it)
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    init {
        viewModelScope.launch {
            try {
                val account = currentAccountWatcher.getAccount()
                val note = noteRepository.find(Note.Id(account.accountId, show.noteId))
                    .getOrThrow()
//                noteRepository.syncConversation(note.id).getOrThrow()
                noteRepository.syncThreadContext(note.id).getOrThrow()
//                recursiveSync(note.id).getOrThrow()
                noteRepository.sync(note.id)
            } catch (e: Exception) {
                Log.w("NoteDetailViewModel", "loadDetail失敗", e)
            }
        }

    }

    suspend fun getUrl(): String {
        val account = currentAccountWatcher.getAccount()
        return "${account.normalizedInstanceUri}/notes/${show.noteId}"
    }


    private fun <T : PlaneNoteViewData> T.capture(): T {
        val self = this
        self.capture(noteCaptureAdapter) {
            it.launchIn(viewModelScope + Dispatchers.IO)
        }
        return this
    }

}

@Suppress("UNCHECKED_CAST")
fun NoteDetailViewModel.Companion.provideFactory(
    factory: NoteDetailViewModel.ViewModelAssistedFactory,
    show: Pageable.Show,
    accountId: Long? = null,
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(show, accountId) as T
    }
}

sealed interface NoteType {
    data class Detail(val note: NoteRelation) : NoteType
    data class Conversation(val note: NoteRelation) : NoteType
    data class Children(val note: NoteRelation, val nextChildren: List<NoteRelation>) : NoteType {


        fun getReplies(): List<NoteRelation> {
            return nextChildren.filter {
                it.note.replyId == note.note.id
            }
        }

    }
}