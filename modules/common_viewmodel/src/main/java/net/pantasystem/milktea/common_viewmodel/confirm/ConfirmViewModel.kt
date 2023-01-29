package net.pantasystem.milktea.common_viewmodel.confirm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import net.pantasystem.milktea.model.confirm.ConfirmCommand
import net.pantasystem.milktea.model.confirm.ConfirmEvent

class ConfirmViewModel : ViewModel(){

    val confirmEvent = MutableSharedFlow<ConfirmCommand>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val confirmedEvent = MutableSharedFlow<ConfirmEvent>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}