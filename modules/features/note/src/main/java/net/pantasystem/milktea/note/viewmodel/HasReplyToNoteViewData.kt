package net.pantasystem.milktea.note.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.setting.LocalConfigRepository

class HasReplyToNoteViewData(
    noteRelation: NoteRelation,
    account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    noteTranslationStore: NoteTranslationStore,
    instanceEmojis: List<Emoji>,
    noteDataSource: NoteDataSource,
    configRepository: LocalConfigRepository,
    coroutineScope: CoroutineScope,
) : PlaneNoteViewData(
    noteRelation,
    account,
    noteCaptureAPIAdapter,
    noteTranslationStore,
    instanceEmojis,
    noteDataSource,
    configRepository,
    coroutineScope
) {
    val reply = noteRelation.reply


    val replyExpanded = MutableLiveData<Boolean>(reply?.note?.cw != null)

    val replyTo = if (reply == null) {
        throw IllegalArgumentException("replyがnullですPlaneNoteViewDataを利用してください")
    } else {
        PlaneNoteViewData(
            reply,
            account,
            noteCaptureAPIAdapter,
            noteTranslationStore,
            instanceEmojis,
            noteDataSource,
            configRepository,
            coroutineScope
        )
    }

    fun expandReplyNote() {
        replyExpanded.value = true
    }


}