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
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache

class NoteDetailViewModel @AssistedInject constructor(
    accountRepository: AccountRepository,
    private val noteRepository: NoteRepository,
    private val noteDataSource: NoteDataSource,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    private val loggerFactory: Logger.Factory,
    private val noteReplyStreaming: ReplyStreaming,
    noteDetailNotesBuilderFactory: NoteDetailNotesFlowBuilder.Factory,
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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    val notes = noteDetailNotesBuilderFactory.create(
        cache,
        currentAccountWatcher,
        viewModelScope,
    ).build(
        show,
        note,
        threadContext,
    ).flowOn(Dispatchers.IO)
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

        viewModelScope.launch {
            noteReplyStreaming.connect { currentAccountWatcher.getAccount() }.mapNotNull { reply ->
                logger.debug {
                    "reply:${reply.id}"
                }
                val account = currentAccountWatcher.getAccount()
                val note = noteRepository.find(Note.Id(account.accountId, show.noteId))
                    .getOrThrow()
                val context = noteDataSource.findNoteThreadContext(note.id).getOrThrow()
                val isRelatedReply = context.descendants.any {
                    it.id == reply.id
                } || note.id == reply.replyId
                if (isRelatedReply) {
                    val updatedContext = context.copy(
                        descendants = context.descendants + reply
                    )
                    noteDataSource.addNoteThreadContext(note.id, updatedContext).getOrThrow()
                }
            }.catch {
                logger.error("observe reply error", it)
            }.collect()
        }

    }

    suspend fun getUrl(): String {
        val account = currentAccountWatcher.getAccount()
        return "${account.normalizedInstanceUri}/notes/${show.noteId}"
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