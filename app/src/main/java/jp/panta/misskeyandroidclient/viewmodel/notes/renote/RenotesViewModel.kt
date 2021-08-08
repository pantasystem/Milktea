package jp.panta.misskeyandroidclient.viewmodel.notes.renote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.renote.Renote
import jp.panta.misskeyandroidclient.model.notes.renote.RenotesPagingService
import jp.panta.misskeyandroidclient.model.notes.renote.createRenotesPagingService
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RenotesViewModel(
    private val renotesPagingService: RenotesPagingService
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val targetNoteId: Note.Id, private val miCore: MiCore) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RenotesViewModel(
                miCore.createRenotesPagingService(targetNoteId)
            ) as T
        }
    }

    val renotes = renotesPagingService.state.map {
        it.convert { list ->
            list.mapNotNull { r ->
                r as? Renote.Normal
            }
        }
    }

    val quotes = renotesPagingService.state.map {
        it.convert { list ->
            list.mapNotNull { r ->
                r as? Renote.Quote
            }
        }
    }

    private val _errors = MutableStateFlow<Throwable?>(null)
    val errors: Flow<Throwable?> = _errors

    fun next() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                renotesPagingService.next()
            }.onFailure {
                _errors.value = it
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                renotesPagingService.refresh()
            }.onFailure {
                _errors.value = it
            }
        }
    }
}