package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.report.Report
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


sealed interface ReportState {
    data class Specify(
        val userId: User.Id,
        val comment: String
    ) : ReportState {
        val canSend: Boolean
            get() = this.comment.isNotBlank()
    }
    object None : ReportState


    sealed interface Sending : ReportState{
        val userId: User.Id
        val comment: String
        data class Doing(
            override val userId: User.Id,
            override val comment: String
        ) : Sending

        data class Failed(
            override val userId: User.Id,
            override val comment: String
        ) : Sending

        data class Success(
            override val userId: User.Id,
            override val comment: String
        ) : Sending
    }




}
class ReportViewModel(private val miCore: MiCore) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportViewModel(miCore) as T
        }
    }

    private val _state = MutableStateFlow<ReportState>(ReportState.None)
    val state: StateFlow<ReportState> = _state

    val comment = state.map {
        (it as? ReportState.Specify)?.comment
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userId = state.map {
        (it as? ReportState.Specify)?.userId
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val canSend = state.map {
        it is ReportState.Specify && it.canSend
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)


    fun changeComment(text: String?) {
        if(state.value is ReportState.Sending) {
            return
        }
        _state.value = (state.value as? ReportState.Specify)?.copy(
            comment = text ?: ""
        ) ?: ReportState.None
    }


    fun clear() {
        _state.value = ReportState.None
    }

    fun newState(userId: User.Id, comment: String?) {
        _state.value = ReportState.Specify(userId, comment?: "")
    }

    fun submit() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                when (val report = state.value) {
                    is ReportState.None -> {
                        return@launch
                    }
                    is ReportState.Sending -> {
                        return@launch
                    }
                    is ReportState.Specify -> {
                        withContext(Dispatchers.Main) {
                            _state.value = ReportState.Sending.Doing(report.userId, report.comment)
                        }
                        val r = Report(
                            report.userId,
                            report.comment
                        )
                        miCore.getUserRepository().report(
                            r
                        )
                        r
                    }
                }

            }.onSuccess {
                withContext(Dispatchers.Main) {
                    _state.value = ReportState.Sending.Success(it.userId, it.comment)
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    val sending = _state.value as ReportState.Sending
                    _state.value = ReportState.Sending.Failed(sending.userId, sending.comment)
                }
            }

        }

    }
}