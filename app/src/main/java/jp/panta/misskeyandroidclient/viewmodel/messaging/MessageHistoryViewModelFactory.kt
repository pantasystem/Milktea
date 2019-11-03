package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.lang.IllegalArgumentException

class MessageHistoryViewModelFactory(
    private val connectionInstance: ConnectionInstance,
    private val miApplication: MiApplication

) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == MessageHistoryViewModel::class.java){
            val misskeyAPI = miApplication.misskeyAPIService!!
            return MessageHistoryViewModel(connectionInstance, misskeyAPI) as T
        }
        throw IllegalArgumentException("error")
    }
}