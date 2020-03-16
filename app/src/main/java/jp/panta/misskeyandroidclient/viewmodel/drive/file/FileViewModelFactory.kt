package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.IllegalArgumentException


@Suppress("UNCHECKED_CAST")
class FileViewModelFactory(
    private val accountRelation: AccountRelation,
    private val miApplication: MiApplication,
    private val selectedFileViewDataMapLiveData: MutableLiveData<Map<String, FileViewData>>? = null,
    private val maxSelectableItemSize: Int = 0,
    private val folderId: String? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == FileViewModel::class.java){
            val misskeyAPI = miApplication.getMisskeyAPI(accountRelation)!!
            return FileViewModel(accountRelation, misskeyAPI, selectedFileViewDataMapLiveData, maxSelectableItemSize, folderId, miApplication.getEncryption()) as T
        }
        throw IllegalArgumentException("クラスが一致しない")
    }
}