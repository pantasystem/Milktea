package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.Account
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class UserDetailViewModelFactory(
    val account: Account,
    val miApplication: MiApplication,
    val userId: String?,
    val fqcnUserName: String?
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == UserDetailViewModel::class.java){
            return UserDetailViewModel(account, miApplication.getMisskeyAPI(account), userId, fqcnUserName, miApplication.getEncryption(), miApplication) as T
        }
        throw IllegalArgumentException("対応していません")
    }
}