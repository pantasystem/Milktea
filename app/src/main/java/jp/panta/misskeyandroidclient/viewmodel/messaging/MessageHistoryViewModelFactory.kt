package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import java.lang.IllegalArgumentException
import jp.panta.misskeyandroidclient.model.account.Account

@Suppress("UNCHECKED_CAST")
class MessageHistoryViewModelFactory(
    private val account: Account,
    private val miApplication: MiApplication

) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == MessageHistoryViewModel::class.java){
            return MessageHistoryViewModel(account, miApplication) as T
        }
        throw IllegalArgumentException("error")
    }
}