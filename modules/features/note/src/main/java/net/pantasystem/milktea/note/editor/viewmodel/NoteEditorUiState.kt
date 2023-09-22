package net.pantasystem.milktea.note.editor.viewmodel

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.file.from
import net.pantasystem.milktea.model.note.CreateNote
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.PollChoiceState
import net.pantasystem.milktea.model.note.PollEditingState
import net.pantasystem.milktea.model.note.PollExpiresAt
import net.pantasystem.milktea.model.note.ReactionAcceptanceType
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.draft.DraftNoteFile
import net.pantasystem.milktea.model.note.toCreatePoll
import net.pantasystem.milktea.model.user.User
import java.util.Date


data class NoteEditorFormState(
    val text: String? = null,
    val cw: String? = null,
    val hasCw: Boolean = false,
    val isSensitive: Boolean = false,
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
    val reactionAcceptanceType: ReactionAcceptanceType? = null,
) {
    val schedulePostAtAsDate: Date? by lazy {
        schedulePostAt?.let {
            Date(it.toEpochMilliseconds())
        }
    }
}

data class NoteEditorUiState(
    val formState: NoteEditorFormState = NoteEditorFormState(),
    val sendToState: NoteEditorSendToState = NoteEditorSendToState(),
    val poll: PollEditingState? = null,
    val files: List<FilePreviewSource> = emptyList(),
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

        if (this.sendToState.renoteId != null && (
                    currentAccount?.instanceType == Account.InstanceType.MISSKEY
                            || currentAccount?.instanceType == Account.InstanceType.FIREFISH
                    )
        ) {
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
        cw = if (formState.hasCw) (formState.cw ?: "") else null,
        viaMobile = false,
        files = files.map {
            it.file
        },
        replyId = sendToState.replyId,
        renoteId = sendToState.renoteId,
        poll = poll?.toCreatePoll(),
        draftNoteId = sendToState.draftNoteId,
        channelId = sendToState.channelId,
        scheduleWillPostAt = sendToState.schedulePostAt,
        isSensitive = formState.isSensitive,
        reactionAcceptance = sendToState.reactionAcceptanceType
    )
}


fun DraftNote.toNoteEditingState(): NoteEditorUiState {
    return NoteEditorUiState(
        formState = NoteEditorFormState(
            text = this.text,
            cw = this.cw,
            hasCw = this.cw != null,
            isSensitive = this.isSensitive ?: false,
        ),
        sendToState = NoteEditorSendToState(
            draftNoteId = this.draftNoteId,
            visibility = Visibility(
                type = this.visibility,
                isLocalOnly = this.localOnly ?: false,
                visibleUserIds = this.visibleUserIds?.map {
                    User.Id(accountId = accountId, id = it)
                }),
            replyId = this.replyId?.let {
                Note.Id(accountId = accountId, noteId = it)
            },
            renoteId = this.renoteId?.let {
                Note.Id(accountId = accountId, noteId = it)
            },
            channelId = channelId,
            schedulePostAt = reservationPostingAt?.let {
                Instant.fromEpochMilliseconds(it.time)
            },
            reactionAcceptanceType = reactionAcceptanceType,
        ),
        poll = this.draftPoll?.let {
            PollEditingState(
                choices = it.choices.map { choice ->
                    PollChoiceState(choice)
                },
                expiresAt = it.expiresAt?.let { ex ->
                    PollExpiresAt.DateAndTime(
                        Instant.fromEpochMilliseconds(ex)
                    )
                } ?: PollExpiresAt.Infinity,
                multiple = it.multiple
            )
        },
        files = draftFiles?.map {
            when (it) {
                is DraftNoteFile.Local -> {
                    FilePreviewSource.Local(AppFile.from(it) as AppFile.Local)
                }

                is DraftNoteFile.Remote -> {
                    FilePreviewSource.Remote(AppFile.from(it) as AppFile.Remote, it.fileProperty)
                }
            }

        } ?: emptyList(),

        )
}