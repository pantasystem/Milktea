package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

//viewはRecyclerView
class NoteConversationViewData(note: Note, var nextChildren: List<PlaneNoteViewData>?, connectionInstance: ConnectionInstance) : PlaneNoteViewData(note, connectionInstance = connectionInstance){

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