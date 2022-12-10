package net.pantasystem.milktea.note.option

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.notes.NoteState
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.watchAccount
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        const val NOTE_ID = "NoteOptionViewModel.NOTE_ID"
    }

    val logger by lazy {
        loggerFactory.create("NoteOptionViewModel")
    }

    val noteIdFlow = savedStateHandle.getStateFlow<Note.Id?>(NOTE_ID, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val noteState = noteIdFlow.filterNotNull().flatMapLatest {
        suspend {
            loadNoteState(it).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Loading(StateContent.NotExist())
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val note = noteIdFlow.filterNotNull().flatMapLatest {
        noteRepository.observeOne(it)
    }.filterNotNull().map {
        noteRelationGetter.get(it).getOrThrow()
    }.catch {

    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAccount = noteIdFlow.filterNotNull().flatMapLatest {
        accountRepository.watchAccount(it.accountId)
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState = combine(noteIdFlow, noteState, note, currentAccount) { id, state, note, ac ->
        NoteOptionUiState(
            noteId = id,
            noteState = state,
            note = note?.note,
            isMyNote = note?.note?.userId?.id == ac?.remoteId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteOptionUiState())

    private suspend fun loadNoteState(id: Note.Id): Result<NoteState> = runCatching {
        withContext(Dispatchers.IO) {
            val account = accountRepository.get(id.accountId).getOrThrow()
            misskeyAPIProvider.get(account.instanceDomain).noteState(
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
    val noteState: ResultState<NoteState> = ResultState.Loading(StateContent.NotExist()),
    val note: Note? = null,
    val noteRelation: NoteRelation? = null,
    val isMyNote: Boolean = false,
)