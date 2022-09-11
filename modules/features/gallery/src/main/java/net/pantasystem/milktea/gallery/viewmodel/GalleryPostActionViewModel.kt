package net.pantasystem.milktea.gallery.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow


sealed class Action {
    object OpenCreationEditor : Action()
}

class GalleryPostActionViewModel : ViewModel() {

    val viewAction = MutableSharedFlow<Action>()


    fun showEditor() {
        viewAction.tryEmit(Action.OpenCreationEditor)
    }
}