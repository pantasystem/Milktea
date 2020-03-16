package jp.panta.misskeyandroidclient.viewmodel.drive.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class FolderViewModelFactory(private val accountRelation: AccountRelation, private val miApplication: MiApplication, private val folderId: String?) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == FolderViewModel::class.java){
            val api = miApplication.getMisskeyAPI(accountRelation.getCurrentConnectionInformation()!!)
            return FolderViewModel(accountRelation, api, folderId, miApplication.getEncryption()) as T
        }
        throw IllegalArgumentException("FolderViewModel::class.javaを指定してください")
    }
}