package net.pantasystem.milktea.gallery.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


sealed class Action {
    object OpenCreationEditor : Action()
}

class GalleryPostActionViewModel : ViewModel() {

    private val _viewAction = MutableSharedFlow<Action>(extraBufferCapacity = 10)
    val viewAction = _viewAction.asSharedFlow()


    fun showEditor() {
        _viewAction.tryEmit(Action.OpenCreationEditor)
    }
}