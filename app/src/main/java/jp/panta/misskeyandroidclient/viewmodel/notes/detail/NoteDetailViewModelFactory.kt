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
    val account: Account,
    val miApplication: MiApplication,
    val show: Pageable.Show
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NoteDetailViewModel::class.java){
            return NoteDetailViewModel(account, show, miApplication, encryption = miApplication.getEncryption()) as T
        }
        throw  IllegalArgumentException("use NoteDetailViewModel::class.java")
    }
}