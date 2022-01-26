package jp.panta.misskeyandroidclient.ui.messaging.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Suppress("UNCHECKED_CAST")
class MessageHistoryViewModelFactory(
    private val miCore: MiCore

) : ViewModelProvider.Factory{
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == MessageHistoryViewModel::class.java){
            return MessageHistoryViewModel(miCore) as T
        }
        throw IllegalArgumentException("error")
    }
}