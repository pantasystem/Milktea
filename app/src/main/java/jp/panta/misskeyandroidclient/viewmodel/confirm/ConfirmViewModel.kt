package jp.panta.misskeyandroidclient.viewmodel.confirm

import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.confirm.ConfirmCommand
import jp.panta.misskeyandroidclient.model.confirm.ConfirmEvent
import jp.panta.misskeyandroidclient.util.eventbus.EventBus

class ConfirmViewModel : ViewModel(){

    val confirmEvent = EventBus<ConfirmCommand>()
    val confirmedEvent = EventBus<ConfirmEvent>()
}