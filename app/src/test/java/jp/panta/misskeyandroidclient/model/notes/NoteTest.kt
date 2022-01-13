package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.model.users.User
import junit.framework.TestCase
import kotlinx.datetime.Clock
import org.junit.Assert

class NoteTest : TestCase() {

    private val note: Note by lazy {
        Note(
            Note.Id(0L, "noteId"),
            Clock.System.now(),
            null, null, User.Id(0L, ""),
            null, null,
            false,
            Visibility.Public(false),
            false,
            null,
            null,
            null,
            0,
            emptyList(),
            emptyList(),
            0,
            null,
            null,
            null,
            null
        )
    }

    fun testIsQuoteWhenTextOnly() {
        val note = note.copy(
            text = "hoge",
            renoteId = Note.Id(0L, "id")
        )
        Assert.assertTrue(note.isQuote())
    }

    fun testIsRenote() {
        val note = note.copy(
            renoteId = Note.Id(0L, "id")
        )
        Assert.assertTrue(note.isRenote())
    }

    fun testHasContentWhenFileOnly() {
        val note = note.copy(
            fileIds = listOf(FileProperty.Id(0L, ""))
        )
        Assert.assertTrue(note.hasContent())
    }

    fun testHasContentWhenTextOnly() {
        val note = note.copy(
            text = "hoge"
        )
        Assert.assertTrue(note.hasContent())
    }

    fun testHasContentWhenPollOnly() {
        val note = note.copy(
            poll = Poll(
                choices = listOf(Poll.Choice("a", 0, false)),
                expiresAt = null,
                multiple = false
            )
        )
        Assert.assertTrue(note.hasContent())
    }

    fun testNotHasContentWhenCwOnly() {
        val note = note.copy(
            cw = "hoge"
        )
        Assert.assertFalse(note.hasContent())

    }

}