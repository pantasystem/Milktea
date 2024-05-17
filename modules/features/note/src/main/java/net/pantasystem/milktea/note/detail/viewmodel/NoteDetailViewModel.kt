package net.pantasystem.milktea.note.detail.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.GetShareNoteUrlUseCase
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.note.NoteRepository
import net.pantasystem.milktea.model.note.NoteThreadContext
import net.pantasystem.milktea.model.note.ReplyStreaming
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    accountRepository: AccountRepository,
    private val noteRepository: NoteRepository,
    private val noteDataSource: NoteDataSource,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    private val loggerFactory: Logger.Factory,
    private val noteReplyStreaming: ReplyStreaming,
    noteDetailNotesBuilderFactory: NoteDetailNotesFlowBuilder.Factory,
    private val savedStateHandle: SavedStateHandle,
    private val getShareNoteUrl: GetShareNoteUrlUseCase,
) : ViewModel() {

    companion object {
        const val EXTRA_NOTE_ID =
            "jp.panta.misskeyandroidclinet.view.notes.detail.EXTRA_NOTE_ID"
        const val EXTRA_ACCOUNT_ID =
            "jp.panta.misskeyandroidclient.view.notes.detail.EXTRA_ACCOUNT_ID"
    }

    val pageable by lazy {
        Pageable.Show(
            requireNotNull(savedStateHandle[EXTRA_NOTE_ID]),
        )
    }

    val accountId: Long? by lazy {
        savedStateHandle[EXTRA_ACCOUNT_ID]
    }


    private val logger by lazy {
        loggerFactory.create("NoteDetailVM")
    }


    private val currentAccountWatcher: CurrentAccountWatcher =
        CurrentAccountWatcher(savedStateHandle[EXTRA_ACCOUNT_ID], accountRepository)

    private val cache =
        planeNoteViewDataCacheFactory.create(currentAccountWatcher::getAccount, viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val note = suspend {
        currentAccountWatcher.getAccount()
    }.asFlow().flatMapLatest {
        noteRepository.observeOne(Note.Id(it.accountId, pageable.noteId))
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
        Pageable.Show(requireNotNull(savedStateHandle[EXTRA_NOTE_ID])),
        note,
        threadContext,
    ).flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    init {
        viewModelScope.launch {
            try {
                val account = currentAccountWatcher.getAccount()
                val note = noteRepository.find(Note.Id(account.accountId, pageable.noteId))
                    .getOrThrow()
//                noteRepository.syncConversation(note.id).getOrThrow()
                noteRepository.syncThreadContext(note.id).getOrThrow()
//                recursiveSync(note.id).getOrThrow()
                noteRepository.sync(note.id).getOrThrow()
            } catch (e: Exception) {
                Log.w("NoteDetailViewModel", "loadDetail失敗", e)
            }
        }

        viewModelScope.launch {
            noteReplyStreaming.connect { currentAccountWatcher.getAccount() }.catch {
                logger.error("observe reply error", it)
            }.collect()
        }

    }

    suspend fun getUrl(): Result<String> = runCancellableCatching {
        val account = currentAccountWatcher.getAccount()
        val note = noteRepository.find(Note.Id(account.accountId, pageable.noteId))
            .getOrThrow()
        getShareNoteUrl(note.id).getOrThrow()
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