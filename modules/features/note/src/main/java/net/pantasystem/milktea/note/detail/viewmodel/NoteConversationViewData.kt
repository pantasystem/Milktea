package net.pantasystem.milktea.note.detail.viewmodel

import androidx.lifecycle.MutableLiveData
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

//viewはRecyclerView
class NoteConversationViewData(
    noteRelation: NoteRelation, var nextChildren: List<PlaneNoteViewData>?, account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    translationStore: NoteTranslationStore
) : PlaneNoteViewData(noteRelation, account, noteCaptureAPIAdapter, translationStore) {
    val conversation = MutableLiveData<List<PlaneNoteViewData>>()
    val hasConversation = MutableLiveData<Boolean>()

    fun getNextNoteForConversation(): PlaneNoteViewData? {
        val filteredRenotes = nextChildren?.filter {
            it.subNote?.note?.id != this.id
        }

        if (filteredRenotes?.size == 1) {
            return filteredRenotes.first()
        }
        return null
    }
}