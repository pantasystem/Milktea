package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class UserDetailViewModelFactory(
    val miApplication: MiApplication,
    val userId: User.Id?,
    val fqcnUserName: String?
) : ViewModelProvider.Factory{
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == UserDetailViewModel::class.java){
            return UserDetailViewModel(userId, fqcnUserName, miApplication, translationStore = miApplication.getTranslationStore()) as T
        }
        throw IllegalArgumentException("対応していません")
    }
}