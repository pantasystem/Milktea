package net.pantasystem.milktea.note.timeline.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import javax.inject.Inject

@HiltViewModel
class TimeMachineEventViewModel @Inject constructor() : ViewModel() {

    private val _loadEvents = MutableSharedFlow<Instant>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1)

    val loadEvents: SharedFlow<Instant> = _loadEvents

    fun setDateTime(instant: Instant) {
        _loadEvents.tryEmit(instant)
    }
}