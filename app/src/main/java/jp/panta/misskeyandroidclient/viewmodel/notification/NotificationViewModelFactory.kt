package jp.panta.misskeyandroidclient.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.Account
import java.lang.ClassCastException


@Suppress("UNCHECKED_CAST")
class NotificationViewModelFactory(
    private val account: Account,
    private val miApplication: MiApplication) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NotificationViewModel::class.java){
            //val noteCapture = miApplication.noteCapture
            val misskeyAPI = miApplication.getMisskeyAPI(account)

            return NotificationViewModel(account, misskeyAPI, miApplication) as T
        }
        throw ClassCastException("不正なクラス")
    }
}