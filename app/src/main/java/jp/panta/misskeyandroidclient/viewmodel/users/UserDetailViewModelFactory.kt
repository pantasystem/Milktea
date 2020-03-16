package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class UserDetailViewModelFactory(
    val accountRelation: AccountRelation,
    val miApplication: MiApplication,
    val userId: String?,
    val fqcnUserName: String?
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == UserDetailViewModel::class.java){
            return UserDetailViewModel(accountRelation, miApplication.getMisskeyAPI(accountRelation.getCurrentConnectionInformation()!!), userId, fqcnUserName, miApplication.getEncryption()) as T
        }
        throw IllegalArgumentException("対応していません")
    }
}