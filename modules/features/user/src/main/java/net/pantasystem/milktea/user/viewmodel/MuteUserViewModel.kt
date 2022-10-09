package net.pantasystem.milktea.user.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class MuteUserViewModel @Inject constructor(): ViewModel() {
    var state by mutableStateOf<SpecifyUserMuteUiState>(SpecifyUserMuteUiState.IndefinitePeriod)
        private set

    fun setDate(year: Int, month: Int, monthOfDay: Int) {

    }

    fun setTime(time: Int, hour: Int) {

    }

    fun onConfirmed() {
        state = SpecifyUserMuteUiState.IndefinitePeriod
    }

    fun onUpdateState(state: SpecifyUserMuteUiState) {
        this.state = state
    }
}

sealed interface SpecifyUserMuteUiState {
    object IndefinitePeriod : SpecifyUserMuteUiState
    data class Specified(val dateTime: Instant) : SpecifyUserMuteUiState {
        val localDateTime by lazy {
            dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
        }

        fun applyDuration(duration: Duration): Specified {
            return copy(dateTime = Clock.System.now() + duration)
        }
    }
}