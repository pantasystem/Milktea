package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLength
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

//viewはRecyclerView
class NoteConversationViewData(note: Note, var nextChildren: List<PlaneNoteViewData>?, account: Account, determineTextLength: DetermineTextLength) : PlaneNoteViewData(note, account, determineTextLength){

    val conversation = MutableLiveData<List<PlaneNoteViewData>>()
    val hasConversation = MutableLiveData<Boolean>()

    fun getNextNoteForConversation(): PlaneNoteViewData?{
        val filteredRenotes = nextChildren?.filter{
            it.subNote?.id != this.id
        }

        if(filteredRenotes?.size == 1){
            return filteredRenotes.first()
        }
        return null
    }
}