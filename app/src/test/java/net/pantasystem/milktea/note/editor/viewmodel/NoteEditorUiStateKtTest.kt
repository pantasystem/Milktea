package net.pantasystem.milktea.note.editor.viewmodel

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftPoll
import net.pantasystem.milktea.model.notes.expiresAt
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days

class NoteEditorUiStateKtTest {

    @Test
    fun draftNote_toNoteEditingState_GiveHasExpiredPoll() {
        val expiresAt = Clock.System.now() + 2.days
        val draftNote = DraftNote(
            accountId = 0,
            visibility = "public",
            visibleUserIds = listOf(),
            text = null,
            cw = null,
            draftFiles = listOf(),
            viaMobile = null,
            localOnly = null,
            noExtractMentions = null,
            noExtractHashtags = null,
            noExtractEmojis = null,
            replyId = null,
            renoteId = null,
            draftPoll = DraftPoll(
                choices = listOf(
                    "A",
                    "B",
                    "C",
                    "D"
                ), multiple = false, expiresAt = expiresAt.toEpochMilliseconds()
            ),
            reservationPostingAt = null,
            channelId = null,
            isSensitive = null,
            draftNoteId = 0

        )
        val state = draftNote.toNoteEditingState()
        Assertions.assertEquals(
            expiresAt.toEpochMilliseconds(),
            state.poll?.expiresAt?.expiresAt()?.toEpochMilliseconds()
        )
        Assertions.assertEquals(listOf(
            "A",
            "B",
            "C",
            "D"
        ), state.poll?.choices?.map {
            it.text
        })
    }
}