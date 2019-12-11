package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.lang.IllegalArgumentException

class UserDetailViewModelFactory(
    val connectionInstance: ConnectionInstance,
    val miApplication: MiApplication,
    val userId: String?,
    val fqcnUserName: String?
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == UserDetailViewModel::class.java){
            return UserDetailViewModel(connectionInstance, miApplication.misskeyAPIService!!, userId, fqcnUserName) as T
        }
        throw IllegalArgumentException("対応していません")
    }
}