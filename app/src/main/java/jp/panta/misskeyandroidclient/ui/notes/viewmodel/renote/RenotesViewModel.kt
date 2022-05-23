package jp.panta.misskeyandroidclient.ui.notes.viewmodel.renote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.di.module.createRenotesPagingService
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.renote.Renote
import net.pantasystem.milktea.data.infrastructure.notes.renote.RenotesPagingService
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState

class RenotesViewModel(
    private val renotesPagingService: RenotesPagingService,
    private val noteGetter: NoteRelationGetter,
    loggerFactory: Logger.Factory
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val targetNoteId: Note.Id, private val miCore: MiCore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RenotesViewModel(
                miCore.createRenotesPagingService(targetNoteId),
                miCore.getGetters().noteRelationGetter,
                miCore.loggerFactory
            ) as T
        }
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

    private fun<T : Renote> Flow<PageableState<List<T>>>.asNoteRelation() : Flow<PageableState<List<NoteRelation>>> {
        return this.map{ pageable ->
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