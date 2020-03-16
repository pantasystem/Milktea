package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.messaging.Message
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class MessageViewModelFactory(
    private val accountRelation: AccountRelation,
    private val miApplication: MiApplication,
    private val messageHistory: Message
): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == MessageViewModel::class.java){
            val misskeyAPI = miApplication.getMisskeyAPI(accountRelation.getCurrentConnectionInformation()!!)
            return MessageViewModel(accountRelation, misskeyAPI, messageHistory, miApplication.getEncryption()) as T
        }
        throw IllegalArgumentException("use MessageViewModel::class.java")
    }
}