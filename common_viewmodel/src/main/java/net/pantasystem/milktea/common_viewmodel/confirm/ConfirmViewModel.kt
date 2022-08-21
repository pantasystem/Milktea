package net.pantasystem.milktea.common_viewmodel.confirm

import androidx.lifecycle.ViewModel
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.model.confirm.ConfirmCommand
import net.pantasystem.milktea.model.confirm.ConfirmEvent

class ConfirmViewModel : ViewModel(){

    val confirmEvent = EventBus<ConfirmCommand>()
    val confirmedEvent = EventBus<ConfirmEvent>()
}