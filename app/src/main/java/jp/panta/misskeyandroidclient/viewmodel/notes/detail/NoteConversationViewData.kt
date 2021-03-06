package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLength
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

//viewはRecyclerView
class NoteConversationViewData(noteRelation: NoteRelation, var nextChildren: List<PlaneNoteViewData>?, account: Account, determineTextLength: DetermineTextLength,
                               noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
)  : PlaneNoteViewData(noteRelation, account, determineTextLength, noteCaptureAPIAdapter){
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