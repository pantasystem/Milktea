package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteEditorViewModelFactory(
    private val miApplication: MiApplication,
    private val replyToNoteId: Note.Id? = null,
    private val quoteToNoteId: Note.Id? = null,
    private val draftNote: DraftNote? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == NoteEditorViewModel::class.java){
            return NoteEditorViewModel(
                miApplication,
                draftNoteDao = miApplication.draftNoteDao,
                replyId = replyToNoteId,
                quoteToNoteId = quoteToNoteId,
                loggerFactory = miApplication.loggerFactory,
                dn = draftNote
            ) as T
        }
        throw IllegalArgumentException("use NoteEditorViewModel::class.java")
    }
}