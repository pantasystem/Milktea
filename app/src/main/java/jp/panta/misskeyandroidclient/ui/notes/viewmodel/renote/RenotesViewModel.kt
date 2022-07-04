package jp.panta.misskeyandroidclient.ui.notes.viewmodel.renote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.data.infrastructure.notes.renote.RenotesPagingService
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.renote.Renote

class RenotesViewModel @AssistedInject constructor(
    private val renotesPagingServiceFactory: RenotesPagingService.Factory,
    private val noteGetter: NoteRelationGetter,
    loggerFactory: Logger.Factory,
    @Assisted val noteId: Note.Id,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(noteId: Note.Id): RenotesViewModel
    }

    companion object;

    private val renotesPagingService by lazy {
        renotesPagingServiceFactory.create(noteId)
    }


    private val logger = loggerFactory.create("RenotesVM")

    val renotes = renotesPagingService.state.map {
        it.convert { list ->
            list.filterIsInstance<Renote.Normal>()
        }
    }.asNoteRelation()


    private val _errors = MutableStateFlow<Throwable?>(null)

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

    private fun <T : Renote> Flow<PageableState<List<T>>>.asNoteRelation(): Flow<PageableState<List<NoteRelation>>> {
        return this.map { pageable ->
            pageable.suspendConvert { list ->
                list.mapNotNull {
                    runCatching {
                        noteGetter.get(it.noteId)
                    }.getOrNull()
                }
            }
        }
    }
}

fun RenotesViewModel.Companion.provideViewModel(
    factory: RenotesViewModel.ViewModelAssistedFactory,
    noteId: Note.Id
) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(noteId) as T
    }
}