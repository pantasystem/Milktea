package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.ClassCastException

@Suppress("UNCHECKED_CAST")
class NotesViewModelFactory(private val miApplication: MiApplication) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NotesViewModel::class.java){
            return NotesViewModel(miApplication, miApplication.reactionHistoryDao) as T
        }
        throw ClassCastException("知らないこだなぁ～？？？")
    }
}