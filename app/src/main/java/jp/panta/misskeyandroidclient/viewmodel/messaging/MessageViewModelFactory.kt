package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class MessageViewModelFactory(
    private val account: Account,
    private val miApplication: MiApplication,
    private val messageHistory: MessageDTO
): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == MessageViewModel::class.java){
            val misskeyAPI = miApplication.getMisskeyAPI(account)
            return MessageViewModel(account, misskeyAPI, messageHistory, miApplication) as T
        }
        throw IllegalArgumentException("use MessageViewModel::class.java")
    }
}