package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteDetailViewModelFactory(
    val show: Pageable.Show,
    val miApplication: MiApplication,
    val accountId: Long? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NoteDetailViewModel::class.java){
            return NoteDetailViewModel(show, miApplication, accountId = accountId, encryption = miApplication.getEncryption()) as T
        }
        throw  IllegalArgumentException("use NoteDetailViewModel::class.java")
    }
}