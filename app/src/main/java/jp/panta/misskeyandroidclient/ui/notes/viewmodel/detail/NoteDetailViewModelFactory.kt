package jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import net.pantasystem.milktea.model.account.page.Pageable
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteDetailViewModelFactory(
    val show: Pageable.Show,
    val miApplication: MiApplication,
    val accountId: Long? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == NoteDetailViewModel::class.java){
            return NoteDetailViewModel(show, miApplication, accountId = accountId, encryption = miApplication.getEncryption()) as T
        }
        throw  IllegalArgumentException("use NoteDetailViewModel::class.java")
    }
}