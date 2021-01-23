package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.api.notes.Note
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteEditorViewModelFactory(
    private val miApplication: MiApplication,
    private val replyToNoteId: String? = null,
    private val quoteToNoteId: String? = null,
    private val note: Note? = null,
    private val draftNote: DraftNote? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NoteEditorViewModel::class.java){
            return NoteEditorViewModel(
                miApplication,
                replyToNoteId = replyToNoteId,
                quoteToNoteId = quoteToNoteId,
                encryption = miApplication.getEncryption(),
                note = note,
                draftNoteDao = miApplication.draftNoteDao,
                draftNote = draftNote
            ) as T
        }
        throw IllegalArgumentException("use NoteEditorViewModel::class.java")
    }
}