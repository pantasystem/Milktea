package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import java.lang.IllegalArgumentException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty


@Suppress("UNCHECKED_CAST")
class FileViewModelFactory(
    private val account: Account,
    private val miApplication: MiApplication,
    private val selectedFileViewDataMapLiveData: MutableLiveData<Map<FileProperty.Id, FileViewData>>? = null,
    private val maxSelectableItemSize: Int = 0,
    private val folderId: String? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == FileViewModel::class.java){
            val misskeyAPI = miApplication.getMisskeyAPI(account)
            return FileViewModel(account, misskeyAPI, selectedFileViewDataMapLiveData, maxSelectableItemSize, folderId, miApplication.getEncryption()) as T
        }
        throw IllegalArgumentException("クラスが一致しない")
    }
}