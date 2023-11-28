package net.pantasystem.milktea.note.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.setting.LocalConfigRepository

class HasReplyToNoteViewData(
    noteRelation: NoteRelation,
    account: Account,
    noteTranslationStore: NoteTranslationStore,
    noteDataSource: NoteDataSource,
    configRepository: LocalConfigRepository,
    coroutineScope: CoroutineScope,
) : PlaneNoteViewData(
    noteRelation,
    account,
    noteTranslationStore,
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
            noteTranslationStore,
            noteDataSource,
            configRepository,
            coroutineScope
        )
    }

    fun expandReplyNote() {
        replyExpanded.value = true
    }


}