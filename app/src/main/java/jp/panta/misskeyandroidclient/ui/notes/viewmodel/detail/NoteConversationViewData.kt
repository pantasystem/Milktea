package jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail

import androidx.lifecycle.MutableLiveData
import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteTranslationStore

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