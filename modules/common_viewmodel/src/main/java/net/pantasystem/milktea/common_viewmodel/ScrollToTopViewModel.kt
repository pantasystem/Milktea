package net.pantasystem.milktea.common_viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class ScrollToTopViewModel @Inject constructor() : ViewModel() {

    private val _scrollToTopEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 10, replay = 0)
    val scrollToTopEvent = _scrollToTopEvent.asSharedFlow()

    fun scrollToTop() {
        _scrollToTopEvent.tryEmit(Unit)
    }
}