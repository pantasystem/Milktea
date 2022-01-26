package jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.DetermineTextLength
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

//viewはRecyclerView
class NoteConversationViewData(noteRelation: NoteRelation, var nextChildren: List<PlaneNoteViewData>?, account: Account, determineTextLength: DetermineTextLength,
                               noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
                               translationStore: NoteTranslationStore
)  : PlaneNoteViewData(noteRelation, account, determineTextLength, noteCaptureAPIAdapter, translationStore){
val conversation = MutableLiveData<List<PlaneNoteViewData>>()
val hasConversation = MutableLiveData<Boolean>()

    fun getNextNoteForConversation(): PlaneNoteViewData?{
        val filteredRenotes = nextChildren?.filter{
            it.subNote?.note?.id != this.id
        }

        if(filteredRenotes?.size == 1){
            return filteredRenotes.first()
        }
        return null
    }
}