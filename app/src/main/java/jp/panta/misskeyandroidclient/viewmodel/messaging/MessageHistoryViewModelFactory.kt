package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class MessageHistoryViewModelFactory(
    private val accountRelation: AccountRelation,
    private val miApplication: MiApplication

) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == MessageHistoryViewModel::class.java){
            val misskeyAPI = miApplication.getMisskeyAPI(accountRelation.getCurrentConnectionInformation()!!)
            return MessageHistoryViewModel(accountRelation, misskeyAPI, miApplication.getEncryption()) as T
        }
        throw IllegalArgumentException("error")
    }
}