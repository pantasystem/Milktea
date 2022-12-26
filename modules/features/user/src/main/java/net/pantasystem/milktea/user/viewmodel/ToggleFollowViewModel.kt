package net.pantasystem.milktea.user.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.user.ToggleFollowUseCase
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject


data class ToggleFollowErrorUiState(
    val userId: User.Id,
    val throwable: Throwable,
)
@HiltViewModel
class ToggleFollowViewModel @Inject constructor(
    val toggleFollowUseCase: ToggleFollowUseCase,
) : ViewModel(){

    private val _errors = MutableStateFlow<ToggleFollowErrorUiState?>(null)
    val errors: StateFlow<ToggleFollowErrorUiState?> = _errors

    fun toggleFollow(userId: User.Id){
        viewModelScope.launch {

            toggleFollowUseCase(userId).onFailure {
                _errors.value = ToggleFollowErrorUiState(userId, it)
            }

        }
    }

}