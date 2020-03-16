package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteEditorViewModelFactory(
    private val accountRelation: AccountRelation,
    private val miApplication: MiApplication,
    private val replyToNoteId: String? = null,
    private val quoteToNoteId: String? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NoteEditorViewModel::class.java){
            val meta = miApplication.nowInstanceMeta!!
            val api = miApplication.getMisskeyAPI(accountRelation.getCurrentConnectionInformation()!!)
            return NoteEditorViewModel(accountRelation, api, meta, replyToNoteId = replyToNoteId, quoteToNoteId = quoteToNoteId, encryption = miApplication.getEncryption()) as T
        }
        throw IllegalArgumentException("use NoteEditorViewModel::class.java")
    }
}