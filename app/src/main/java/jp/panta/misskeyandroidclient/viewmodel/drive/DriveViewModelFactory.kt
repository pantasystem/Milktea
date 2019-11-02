package jp.panta.misskeyandroidclient.viewmodel.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.lang.IllegalArgumentException

class DriveViewModelFactory(
    private val connectionInstance: ConnectionInstance,
    private val miApplication: MiApplication
) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == DriveViewModel::class.java){
            val misskeyAPI = miApplication.misskeyAPIService!!
            return DriveViewModel(connectionInstance, misskeyAPI) as T
        }
        throw IllegalArgumentException("DriveViewModel::class.javaを指定してください")
    }
}