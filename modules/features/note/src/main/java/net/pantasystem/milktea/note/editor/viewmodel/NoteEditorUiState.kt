package net.pantasystem.milktea.note.editor.viewmodel

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.notes.*


data class NoteEditorFormState(
    val text: String? = null,
    val cw: String? = null,
    val hasCw: Boolean = false,
)

data class VisibilityAndChannelId(
    val visibility: Visibility = Visibility.Public(false),
    val channelId: Channel.Id? = null,
)

data class NoteEditorSendToState(
    val visibility: Visibility = Visibility.Public(false),
    val channelId: Channel.Id? = null,
    val renoteId: Note.Id? = null,
    val replyId: Note.Id? = null,
    val schedulePostAt: Instant? = null,
    val draftNoteId: Long? = null,
)

data class NoteEditorUiState(
    val formState: NoteEditorFormState = NoteEditorFormState(),
    val sendToState: NoteEditorSendToState = NoteEditorSendToState(),
    val poll: PollEditingState? = null,
    val files: List<AppFile> = emptyList(),
    val currentAccount: Account? = null,
) {
    val totalFilesCount: Int
        get() = this.files.size

    fun checkValidate(textMaxLength: Int = 3000, maxFileCount: Int = 4): Boolean {
        if (this.files.size > maxFileCount) {
            return false
        }

        if ((this.formState.text?.codePointCount(0, this.formState.text.length)
                ?: 0) > textMaxLength
        ) {
            return false
        }

        if (sendToState.channelId != null && sendToState.visibility != Visibility.Public(true)) {
            return false
        }

        if (this.sendToState.renoteId != null) {
            return true
        }
        if (this.poll != null && this.poll.checkValidate()) {
            return true
        }
        return !(
                this.formState.text.isNullOrBlank()
                        && this.files.isEmpty()
                )
    }

    fun shouldDiscardingConfirmation(): Boolean {
        val address = (sendToState.visibility as? Visibility.Specified)?.visibleUserIds
            ?: emptyList()
        return !formState.text.isNullOrBlank()
                || files.isNotEmpty()
                || !poll?.choices.isNullOrEmpty()
                || address.isNotEmpty()
    }
}


fun NoteEditorUiState.toCreateNote(account: Account): CreateNote {
    return CreateNote(
        author = account,
        visibility = sendToState.visibility,
        text = formState.text,
        cw = if (formState.hasCw) formState.cw else null,
        viaMobile = false,
        files = files,
        replyId = sendToState.replyId,
        renoteId = sendToState.renoteId,
        poll = poll?.toCreatePoll(),
        draftNoteId = sendToState.draftNoteId,
        channelId = sendToState.channelId,
        scheduleWillPostAt = sendToState.schedulePostAt,
    )
}
