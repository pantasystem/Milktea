package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.messaging.MessagingId
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Suppress("UNCHECKED_CAST")
class MessageViewModelFactory(
    private val messagingId: MessagingId,
    private val miCore: MiCore
): ViewModelProvider.Factory{


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == MessageViewModel::class.java){
            return MessageViewModel(miCore, messagingId) as T
        }
        throw IllegalArgumentException("use MessageViewModel::class.java")
    }
}