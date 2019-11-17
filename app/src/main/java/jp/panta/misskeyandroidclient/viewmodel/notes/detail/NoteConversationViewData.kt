package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

//viewはRecyclerView
class NoteConversationViewData(note: Note) : PlaneNoteViewData(note){
    val conversation = MutableLiveData<List<PlaneNoteViewData>>()
    val hasConversation = Transformations.map(conversation){
        it.isNotEmpty()
    }
}