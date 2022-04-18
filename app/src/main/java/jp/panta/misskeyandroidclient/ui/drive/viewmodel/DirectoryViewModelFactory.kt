package jp.panta.misskeyandroidclient.ui.drive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.pantasystem.milktea.model.drive.DriveStore
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.account.watchAccount
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class DirectoryViewModelFactory(private val accountId: Long?, private val miCore: MiCore, private val driveStore: net.pantasystem.milktea.model.drive.DriveStore) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == DirectoryViewModel::class.java){
            return DirectoryViewModel(miCore.watchAccount(accountId), driveStore, miCore.getMisskeyAPIProvider(), miCore.getEncryption(), miCore.loggerFactory) as T
        }
        throw IllegalArgumentException("FolderViewModel::class.javaを指定してください")
    }
}