package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.lang.IllegalArgumentException

class NoteEditorViewModelFactory(
    private val connectionInstance: ConnectionInstance,
    private val miApplication: MiApplication,
    private val replyToNoteId: String? = null,
    private val quoteToNoteId: String? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NoteEditorViewModel::class.java){
            val meta = miApplication.nowInstanceMeta!!
            val api = miApplication.misskeyAPIService!!
            return NoteEditorViewModel(connectionInstance, api, meta, replyToNoteId = replyToNoteId, quoteToNoteId = quoteToNoteId) as T
        }
        throw IllegalArgumentException("use NoteEditorViewModel::class.java")
    }
}