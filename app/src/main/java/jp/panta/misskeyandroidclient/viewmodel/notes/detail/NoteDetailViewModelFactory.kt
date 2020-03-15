package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteDetailViewModelFactory(
    val connectionInstance: ConnectionInstance,
    val miApplication: MiApplication,
    val noteId: String
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NoteDetailViewModel::class.java){
            return NoteDetailViewModel(connectionInstance, miApplication.misskeyAPIService!!, noteId, encryption = miApplication.mEncryption) as T
        }
        throw  IllegalArgumentException("use NoteDetailViewModel::class.java")
    }
}