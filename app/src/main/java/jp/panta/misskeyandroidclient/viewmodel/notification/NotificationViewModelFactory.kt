package jp.panta.misskeyandroidclient.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.ClassCastException


@Suppress("UNCHECKED_CAST")
class NotificationViewModelFactory(
    private val miApplication: MiApplication
    ) : ViewModelProvider.Factory{
    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == NotificationViewModel::class.java){
            //val noteCapture = miApplication.noteCapture

            return NotificationViewModel(miApplication) as T
        }
        throw ClassCastException("不正なクラス")
    }
}