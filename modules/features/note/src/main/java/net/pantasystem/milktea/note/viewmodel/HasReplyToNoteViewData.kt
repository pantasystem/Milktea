package net.pantasystem.milktea.note.viewmodel

import androidx.lifecycle.MutableLiveData
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRelation

class HasReplyToNoteViewData(
    noteRelation: NoteRelation,
    account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    noteTranslationStore: NoteTranslationStore,
)  : PlaneNoteViewData(noteRelation, account, noteCaptureAPIAdapter, noteTranslationStore){
    val reply = noteRelation.reply


    val replyExpanded = MutableLiveData<Boolean>(reply?.note?.cw != null)

    val replyTo = if(reply == null){
        throw IllegalArgumentException("replyがnullですPlaneNoteViewDataを利用してください")
    }else{
        PlaneNoteViewData(reply, account, noteCaptureAPIAdapter, noteTranslationStore)
    }

    fun expandReplyNote() {
        replyExpanded.value = true
    }




}