package net.pantasystem.milktea.common_android_ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.report.ReportState
import net.pantasystem.milktea.model.user.report.SendReportUseCase
import javax.inject.Inject




@HiltViewModel
class ReportViewModel @Inject constructor(
    val sendReportUseCase: SendReportUseCase,
): ViewModel(){


    private val _state = MutableStateFlow<ReportState>(ReportState.None)
    val state: StateFlow<ReportState> = _state

    val successOrFailureEvent = state.distinctUntilChangedBy {
        it is ReportState.Sending.Success
                || it is ReportState.Sending.Failed
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000))

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
        _state.value = ReportState.Specify(userId, comment ?: "")
    }

    fun submit() {
        viewModelScope.launch {
            sendReportUseCase.invoke(state.value).collect {
                _state.value = it
            }
        }

    }
}