package net.pantasystem.milktea.note.timeline.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimeMachineDialogViewModel @Inject constructor(): ViewModel() {

    private val _currentDateTime = MutableStateFlow(Clock.System.now())
    val currentDateTime: StateFlow<Instant> = _currentDateTime
    val currentDate = _currentDateTime.map {
        Date(it.toEpochMilliseconds())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Date())

    fun setDate(year: Int, month: Int, dayOfMonth: Int) {

        _currentDateTime.update {
            val calendar = Calendar.getInstance()
            calendar.time = Date(it.toEpochMilliseconds())
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            Instant.fromEpochMilliseconds(calendar.time.time)
        }
    }

    fun setTime(hourOfDay: Int, minutes: Int) {
        _currentDateTime.update {
            val calendar = Calendar.getInstance()
            calendar.time = Date(it.toEpochMilliseconds())
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minutes)
            Instant.fromEpochMilliseconds(calendar.time.time)
        }
    }

}