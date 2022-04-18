package jp.panta.misskeyandroidclient.viewmodel.confirm

import androidx.lifecycle.ViewModel
import net.pantasystem.milktea.data.infrastructure.confirm.ConfirmCommand
import net.pantasystem.milktea.data.infrastructure.confirm.ConfirmEvent
import jp.panta.misskeyandroidclient.util.eventbus.EventBus

class ConfirmViewModel : ViewModel(){

    val confirmEvent = EventBus<ConfirmCommand>()
    val confirmedEvent = EventBus<ConfirmEvent>()
}