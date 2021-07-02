package jp.panta.misskeyandroidclient.viewmodel.drive.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.watchAccount
import jp.panta.misskeyandroidclient.model.drive.DriveStore
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class FolderViewModelFactory(private val accountId: Long?, private val miCore: MiCore, val driveStore: DriveStore) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == DirectoryViewModel::class.java){
            return DirectoryViewModel(miCore.watchAccount(accountId), driveStore, miCore.getMisskeyAPIProvider(), miCore.getEncryption(), miCore.loggerFactory) as T
        }
        throw IllegalArgumentException("FolderViewModel::class.javaを指定してください")
    }
}