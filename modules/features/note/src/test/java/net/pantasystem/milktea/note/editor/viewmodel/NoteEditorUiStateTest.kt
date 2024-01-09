package net.pantasystem.milktea.note.editor.viewmodel

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.note.CreateNote
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.PollChoiceState
import net.pantasystem.milktea.model.note.PollEditingState
import net.pantasystem.milktea.model.note.PollExpiresAt
import net.pantasystem.milktea.model.note.ReactionAcceptanceType
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.draft.DraftPoll
import net.pantasystem.milktea.model.note.poll.CreatePoll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NoteEditorUiStateTest {

    @Test
    fun toCreateNote() {
        val state = NoteEditorUiState(
            formState = NoteEditorFormState(
                text = "text",
                cw = "cw",
                hasCw = true,
                isSensitive = true,
            ),
            sendToState = NoteEditorSendToState(
                visibility = Visibility.Public(true),
                channelId = Channel.Id(0L, "channel"),
                renoteId = Note.Id(0L, "renote"),
                replyId = Note.Id(0L, "reply"),
                schedulePostAt = null,
                draftNoteId = null,
                reactionAcceptanceType = ReactionAcceptanceType.LikeOnly,
            ),
            poll = PollEditingState(
                choices = listOf(
                    PollChoiceState("choice1"),
                    PollChoiceState("choice2"),
                ),
                expiresAt = PollExpiresAt.Infinity,
                multiple = true,
            ),
            files = emptyList(),
            currentAccount = null,
        )

        val account = Account(
            remoteId = "",
            instanceDomain = "",
            userName = "",
            instanceType = Account.InstanceType.MISSKEY,
            token = ""
        )
        val note = state.toCreateNote(
            account
        )
        Assertions.assertEquals(
            CreateNote(
                author = account,
                text = "text",
                cw = "cw",
                isSensitive = true,
                visibility = Visibility.Public(true),
                channelId = Channel.Id(0L, "channel"),
                renoteId = Note.Id(0L, "renote"),
                replyId = Note.Id(0L, "reply"),
                reactionAcceptance = ReactionAcceptanceType.LikeOnly,
                poll = CreatePoll(
                    choices = listOf(
                        "choice1",
                        "choice2",
                    ),
                    multiple = true,
                    expiresAt = null,
                ),
                files = emptyList(),
                viaMobile = false,
            ),
            note,
        )
    }

    @Test
    fun toNoteEditingState_FromDraftNote() {
        val draftNote = DraftNote(
            accountId = 5537,
            visibility = "public",
            visibleUserIds = listOf(),
            text = "texttext",
            cw = "hogepiyo",
            draftFiles = listOf(),
            viaMobile = null,
            localOnly = true,
            noExtractMentions = null,
            noExtractHashtags = null,
            noExtractEmojis = null,
            replyId = "replyId",
            renoteId = "renoteId",
            draftPoll = DraftPoll(
                choices = emptyList(),
                multiple = true,
                expiresAt = null,
            ),
            reservationPostingAt = null,
            channelId = Channel.Id(0L, "channel"),
            isSensitive = null,
            reactionAcceptanceType = ReactionAcceptanceType.LikeOnly,
            draftNoteId = 7742
        )

        val state = draftNote.toNoteEditingState()
        Assertions.assertEquals(
            NoteEditorUiState(
                formState = NoteEditorFormState(
                    text = "texttext",
                    cw = "hogepiyo",
                    hasCw = true,
                    isSensitive = false,
                ),
                sendToState = NoteEditorSendToState(
                    visibility = Visibility.Public(true),
                    channelId = Channel.Id(0L, "channel"),
                    renoteId = Note.Id(5537, "renoteId"),
                    replyId = Note.Id(5537, "replyId"),
                    schedulePostAt = null,
                    draftNoteId = 7742,
                    reactionAcceptanceType = ReactionAcceptanceType.LikeOnly,
                ),
                poll = PollEditingState(
                    choices = emptyList(),
                    expiresAt = PollExpiresAt.Infinity,
                    multiple = true,
                ),
                files = emptyList(),
                currentAccount = null,
            ),
            state,
        )
    }

    @Test
    fun checkValidate_GiveInvalidDataCaseEmptyFields() {
        val state = NoteEditorUiState()
        Assertions.assertFalse(
            state.checkValidate()
        )
    }

    @Test
    fun checkValidate_GiveValidDataCaseSomeText() {
        val state = NoteEditorUiState(
            formState = NoteEditorFormState(
                text = "hogehoge"
            )
        )
        Assertions.assertTrue(
            state.checkValidate()
        )
    }

    @Test
    fun checkValidate_GiveInvalidDataTextOverflow() {
        val state = NoteEditorUiState(
            formState = NoteEditorFormState(
                text = "a".repeat(3001)
            )
        )

        Assertions.assertFalse(
            state.checkValidate(
                textMaxLength = 3000,
            )
        )
    }

    @Test
    fun checkValidate_GiveInvalidDataCaseCwAllowBlank() {
        val state = NoteEditorUiState(
            formState = NoteEditorFormState(
                text = "hogehoge",
                cw = "",
                hasCw = true,
            )
        )

        Assertions.assertFalse(
            state.checkValidate(
                isCwAllowBlank = false,
            )
        )
    }

    @Test
    fun checkValidate_GiveValidFiles() {
        val state = NoteEditorUiState(
            files = listOf(
                FilePreviewSource.Local(
                    file = AppFile.Local(
                        name = "name",
                        path = "path",
                        thumbnailUrl = "thumbnailUrl",
                        type = "type",
                        isSensitive = false,
                        folderId = null,
                        fileSize = null,
                        comment = null,
                        id = 4695,
                    ),
                ),
            )
        )

        Assertions.assertTrue(
            state.checkValidate()
        )
    }

    @Test
    fun checkValidate_GiveInvalidFileSize() {
        val state = NoteEditorUiState(
            files = (0..16).map {
                FilePreviewSource.Local(
                    file = AppFile.Local(
                        name = "name",
                        path = "path",
                        thumbnailUrl = "thumbnailUrl",
                        type = "type",
                        isSensitive = false,
                        folderId = null,
                        fileSize = null,
                        comment = null,
                        id = it.toLong(),
                    ),
                )
            }
        )

        Assertions.assertFalse(
            state.checkValidate(maxFileCount = 16)
        )
    }

    @Test
    fun checkValidate_GiveValidJustFileSize() {
        val state = NoteEditorUiState(
            files = (1..16).map {
                FilePreviewSource.Local(
                    file = AppFile.Local(
                        name = "name",
                        path = "path",
                        thumbnailUrl = "thumbnailUrl",
                        type = "type",
                        isSensitive = false,
                        folderId = null,
                        fileSize = null,
                        comment = null,
                        id = it.toLong(),
                    ),
                )
            }
        )

        Assertions.assertTrue(
            state.checkValidate(maxFileCount = 16)
        )
    }

}