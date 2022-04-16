package jp.panta.misskeyandroidclient.ui.drive.viewmodel.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException
import net.pantasystem.milktea.data.model.account.watchAccount
import net.pantasystem.milktea.data.model.drive.DriveStore
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.account.watchAccount
import kotlinx.coroutines.ExperimentalCoroutinesApi


@Suppress("UNCHECKED_CAST")
class FileViewModelFactory(
    private val accountId :Long?,
    private val miCore: MiCore,
    private val driveStore: DriveStore,
) : ViewModelProvider.Factory{

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == FileViewModel::class.java){
            return FileViewModel(
                miCore.watchAccount(accountId),
                miCore,
                driveStore
            ) as T
        }
        throw IllegalArgumentException("クラスが一致しない")
    }
}