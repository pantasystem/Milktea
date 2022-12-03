package net.pantasystem.milktea.note.renote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.data.infrastructure.notes.renote.RenotesPagingService
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.renote.Renote
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

@HiltViewModel
class RenotesViewModel @Inject constructor(
    private val renotesPagingServiceFactory: RenotesPagingService.Factory,
    private val noteGetter: NoteRelationGetter,
    private val noteRepository: NoteRepository,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    accountStore: AccountStore,
    loggerFactory: Logger.Factory,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


    companion object {
        const val EXTRA_NOTE_ID = "RenotesViewModel.EXTRA_NOTE_ID"
    }
    val noteId: Note.Id = savedStateHandle[EXTRA_NOTE_ID]
        ?: throw IllegalArgumentException()

    private val renotesPagingService by lazy {
        renotesPagingServiceFactory.create(noteId)
    }

    private val logger = loggerFactory.create("RenotesVM")

    val renotes = renotesPagingService.state.map {
        it.convert { list ->
            list.filterIsInstance<Renote.Normal>()
        }
    }.asNoteRelation()

    val myId = accountStore.observeCurrentAccount.map {
        it?.let {
            User.Id(it.accountId, it.remoteId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _errors = MutableStateFlow<Throwable?>(null)

    init {
        viewModelScope.launch {
            renotesPagingService.state.mapNotNull {
                (it.content as? StateContent.Exist)?.rawContent
            }.map { renotes ->
                renotes.map {
                    noteCaptureAPIAdapter.capture(it.noteId)
                }
            }.map { flows ->
                combine(flows) {
                    it.toList()
                }
            }.collect()
        }
    }

    fun next() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                renotesPagingService.next()
            }.onFailure {
                logger.warning("next error", e = it)
                _errors.value = it
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                renotesPagingService.refresh()
            }.onFailure {
                logger.warning("refresh error", e = it)
                _errors.value = it
            }
        }
    }

    fun delete(noteId: Note.Id) {
        viewModelScope.launch {
            noteRepository.delete(noteId).onFailure {
                _errors.value = it
            }.onSuccess {
                refresh()
            }
        }
    }

    private fun <T : Renote> Flow<PageableState<List<T>>>.asNoteRelation(): Flow<PageableState<List<NoteRelation>>> {
        return this.map { pageable ->
            pageable.suspendConvert { list ->
                list.mapNotNull {
                    noteGetter.get(it.noteId).getOrNull()
                }
            }
        }
    }
}