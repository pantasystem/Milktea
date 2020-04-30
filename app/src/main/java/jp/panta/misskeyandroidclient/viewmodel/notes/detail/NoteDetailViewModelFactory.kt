package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteDetailViewModelFactory(
    val accountRelation: AccountRelation,
    val miApplication: MiApplication,
    val show: Page.Show
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NoteDetailViewModel::class.java){
            return NoteDetailViewModel(accountRelation, show, miApplication, encryption = miApplication.getEncryption()) as T
        }
        throw  IllegalArgumentException("use NoteDetailViewModel::class.java")
    }
}