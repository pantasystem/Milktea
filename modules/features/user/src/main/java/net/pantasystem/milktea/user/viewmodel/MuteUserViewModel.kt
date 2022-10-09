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
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class MuteUserViewModel @Inject constructor(): ViewModel() {
    var state by mutableStateOf<SpecifyUserMuteUiState>(SpecifyUserMuteUiState.IndefinitePeriod)
        private set
    val expiredAt: Instant
        get() {
            return (state as? SpecifyUserMuteUiState.Specified)?.dateTime
                ?: Clock.System.now()
        }

    fun setDate(year: Int, month: Int, dayOfMonth: Int) {
        val dateTime = (state as? SpecifyUserMuteUiState.Specified)?.dateTime ?: Clock.System.now()
        val calendar = Calendar.getInstance()
        calendar.time = Date(dateTime.toEpochMilliseconds())
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        state = SpecifyUserMuteUiState.Specified(Instant.fromEpochMilliseconds(calendar.time.time))
    }

    fun setTime(hour: Int, minutes: Int) {
        val dateTime = (state as? SpecifyUserMuteUiState.Specified)?.dateTime ?: Clock.System.now()
        val calendar = Calendar.getInstance()
        calendar.time = Date(dateTime.toEpochMilliseconds())
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minutes)
        state = SpecifyUserMuteUiState.Specified(Instant.fromEpochMilliseconds(calendar.time.time))
    }

    fun onConfirmed() {
        state = SpecifyUserMuteUiState.IndefinitePeriod
    }

    fun onCanceled() {
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