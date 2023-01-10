package net.pantasystem.milktea.note.detail.viewmodel

import androidx.lifecycle.MutableLiveData
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

//viewはRecyclerView
class NoteConversationViewData(
    noteRelation: NoteRelation,
    account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    translationStore: NoteTranslationStore,
    instanceEmojis: List<Emoji>,
) : PlaneNoteViewData(noteRelation, account, noteCaptureAPIAdapter, translationStore, instanceEmojis) {
    val conversation = MutableLiveData<List<PlaneNoteViewData>>()
    val hasConversation = MutableLiveData<Boolean>()

}