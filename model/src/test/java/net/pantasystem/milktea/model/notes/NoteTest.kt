package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.user.User
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTest {

    @Test
    fun isQuote() {
        val note = generateEmptyNote()
        val quoteNote = note.copy(
            renoteId = Note.Id(0L, "noteId"),
            text = "text"
        )

        assertTrue(quoteNote.isQuote())
    }


    @Test
    fun hasContent_OnlyText() {
        assertTrue(generateEmptyNote().copy(text = "text").hasContent())
    }

    @Test
    fun hasContent_OnlyFiles() {
        assertTrue(generateEmptyNote().copy(fileIds = listOf(FileProperty.Id(0L, ""))).hasContent())
    }

    @Test
    fun hasContent_OnlyPoll() {
        val note = generateEmptyNote().copy(
            poll = Poll(choices = emptyList(), multiple = false, expiresAt = null)
        )
        assertTrue(note.hasContent())
    }

    @Test
    fun hasContent_ReturnsFalse() {
        // NOTE: cwだけの場合はcw扱いにならない
        val note = generateEmptyNote().copy(cw = "a")
        assertFalse(note.hasContent())
    }

    @Test
    fun isOwnReaction() {
        val note = generateEmptyNote().copy(
            myReaction = "kawaii"
        )
        assertTrue(note.isOwnReaction(Reaction(":kawaii@.:")))
        assertTrue(note.isOwnReaction(Reaction(":kawaii:")))
    }

    @Test
    fun isOwnReaction_WhenHasNotMyReaction() {
        val note = generateEmptyNote().copy(
            myReaction = null
        )
        assertFalse(note.isOwnReaction(Reaction(":kawaii:")))
    }

    @Test
    fun canRenote_WhenVisibilityPublic() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Public(true)
        )
        assertTrue(note.canRenote(User.Id(accountId = note.id.accountId, "acId")))
    }

    @Test
    fun canRenote_WhenVisibilityHome() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Home(true)
        )
        assertTrue(note.canRenote(User.Id(accountId = note.id.accountId, "acId")))
    }

    @Test
    fun canRenote_WhenVisibilitySpecified() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Specified(emptyList()),
        )
        assertFalse(note.canRenote(User.Id(accountId = note.id.accountId, "acId")))

    }

    @Test
    fun canRenote_WhenVisibilitySpecifiedAndMyNote() {
        val userId = generateEmptyNote().userId
        val note = generateEmptyNote().copy(
            visibility = Visibility.Specified(emptyList()),
            userId = userId,
        )
        assertTrue(note.canRenote(userId))
    }

    @Test
    fun canRenote_GetByOtherAccountId() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Public(false),
        )
        assertFalse(note.canRenote(User.Id(accountId = note.id.accountId + 1L, "acId")))
    }

}