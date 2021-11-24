package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import java.lang.IllegalArgumentException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.CurrentAccountWatcher
import jp.panta.misskeyandroidclient.model.account.watchAccount
import jp.panta.misskeyandroidclient.model.drive.DriveState
import jp.panta.misskeyandroidclient.model.drive.DriveStore
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.SelectedFilePropertyIds
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi


@Suppress("UNCHECKED_CAST")
class FileViewModelFactory(
    private val accountId :Long?,
    private val miCore: MiCore,
    private val driveStore: DriveStore,
) : ViewModelProvider.Factory{

    @ExperimentalCoroutinesApi
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