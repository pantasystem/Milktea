package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.ClassCastException

@Suppress("UNCHECKED_CAST")
class NotesViewModelFactory(private val accountRelation: AccountRelation, private val miApplication: MiApplication) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NotesViewModel::class.java){
            return NotesViewModel(accountRelation, miApplication.misskeyAPIService!!, miApplication.mEncryption, miApplication.reactionHistoryDao) as T
        }
        throw ClassCastException("知らないこだなぁ～？？？")
    }
}