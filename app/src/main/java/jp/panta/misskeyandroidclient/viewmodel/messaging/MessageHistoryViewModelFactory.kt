package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import java.lang.IllegalArgumentException
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Suppress("UNCHECKED_CAST")
class MessageHistoryViewModelFactory(
    private val miApplication: MiApplication

) : ViewModelProvider.Factory{
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == MessageHistoryViewModel::class.java){
            return MessageHistoryViewModel(miApplication) as T
        }
        throw IllegalArgumentException("error")
    }
}