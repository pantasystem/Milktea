package net.pantasystem.milktea.note.option

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.notes.NoteState
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteRepository
import javax.inject.Inject

@HiltViewModel
class NoteOptionViewModel @Inject constructor(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val noteRepository: NoteRepository,
    val noteRelationGetter: NoteRelationGetter,
    val loggerFactory: Logger.Factory,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        const val NOTE_ID = "NoteOptionViewModel.NOTE_ID"
    }

    private val logger by lazy {
        loggerFactory.create("NoteOptionViewModel")
    }

    private val noteIdFlow = savedStateHandle.getStateFlow<Note.Id?>(NOTE_ID, null)

    private val noteState = noteIdFlow.filterNotNull().map { noteId ->
        loadNoteState(noteId).onFailure {
            logger.error("noteState load error", it)
        }.getOrNull()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val note = noteIdFlow.filterNotNull().flatMapLatest {
        noteRepository.observeOne(it)
    }.filterNotNull().map {
        noteRelationGetter.get(it).getOrThrow()
    }.catch {

    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentAccount = noteIdFlow.filterNotNull().map { noteId ->
        accountRepository.get(noteId.accountId).onFailure {
            logger.error("get account error", it)
        }.getOrNull()
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState = combine(noteIdFlow, noteState, note, currentAccount) { id, state, note, ac ->
        NoteOptionUiState(
            noteId = id,
            noteState = state,
            note = note?.note,
            isMyNote = note?.note?.userId?.id == ac?.remoteId,
            currentAccount = ac,
            noteRelation = note
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteOptionUiState())

    init {
        viewModelScope.launch {
            noteIdFlow.filterNotNull().map {
                noteRepository.sync(it)
            }.catch {
                logger.error("sync note error", it)
            }.collect()
        }
    }

    fun createThreadMute(noteId: Note.Id) {
        viewModelScope.launch {
            noteRepository.createThreadMute(noteId).onFailure {
                logger.error("create thread mute failed", it)
            }
            savedStateHandle[NOTE_ID] = noteId
        }
    }

    fun deleteThreadMute(noteId: Note.Id) {
        viewModelScope.launch {
            noteRepository.deleteThreadMute(noteId).onFailure {
                logger.error("delete thread mute failed", it)
            }
            savedStateHandle[NOTE_ID] = noteId
        }
    }

    private suspend fun loadNoteState(id: Note.Id): Result<NoteState> = runCatching {
        withContext(Dispatchers.IO) {
            val account = accountRepository.get(id.accountId).getOrThrow()
            misskeyAPIProvider.get(account.normalizedInstanceDomain).noteState(
                NoteRequest(
                    i = account.token,
                    noteId = id.noteId
                )
            ).throwIfHasError().body()!!
        }
    }

}

data class NoteOptionUiState(
    val noteId: Note.Id? = null,
    val noteState: NoteState? = null,
    val note: Note? = null,
    val noteRelation: NoteRelation? = null,
    val isMyNote: Boolean = false,
    val currentAccount: Account? = null
)