package jp.panta.misskeyandroidclient.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.lang.ClassCastException


@Suppress("UNCHECKED_CAST")
class NotificationViewModelFactory(
    private val connectionInstance: ConnectionInstance,
    private val miApplication: MiApplication) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NotificationViewModel::class.java){
            //val noteCapture = miApplication.noteCapture
            val misskeyAPI = miApplication.misskeyAPIService!!

            return NotificationViewModel(connectionInstance, misskeyAPI, miApplication.encryption) as T
        }
        throw ClassCastException("不正なクラス")
    }
}