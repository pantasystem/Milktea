package net.pantasystem.milktea.data.infrastructure.notes.impl.db

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.make
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class NoteRecordTest {

    @Test
    fun applyModel_GiveCommonStruct() {
        val record = NoteRecord()
        val note = Note.make(
            Note.Id(0L, "nid1"),
            User.Id(0L, "uid1"),
            text = "text-text-text-ttt",
            cw = "cw-test-text",
            replyId = Note.Id(0L, "nid2"),
            renoteId = Note.Id(0L, "nid3"),
            viaMobile = true,
            visibility = Visibility.Public(false),
            localOnly = false,
            visibleUserIds = listOf(User.Id(0L, "uid2"), User.Id(0L, "uid3")),
            uri = "uri-uri-uri-uri",
            url = "url-url-url-url",
            renoteCount = 9,
            reactionCounts = listOf(
                ReactionCount("like", 1),
                ReactionCount("love", 2),
                ReactionCount("haha", 3),
                ReactionCount("wow", 4),
                ReactionCount("sad", 5),
                ReactionCount("angry", 6),
            ),
            emojis = listOf(
                Emoji(name = "name1", url = "url1"),
                Emoji(name = "name2", url = "url2"),
                Emoji(name = "name3", url = "url3"),
            ),
            repliesCount = 10,
            fileIds = listOf(FileProperty.Id(0L, "fid1"), FileProperty.Id(0L, "fid2")),
            poll = null,
            myReaction = "like",
            app = null,
            channelId = null,
        )
        record.applyModel(note)
        Assertions.assertEquals("nid1", record.noteId)
        Assertions.assertEquals("uid1", record.userId)
        Assertions.assertEquals("text-text-text-ttt", record.text)
        Assertions.assertEquals("cw-test-text", record.cw)
        Assertions.assertEquals("nid2", record.replyId)
        Assertions.assertEquals("nid3", record.renoteId)
        Assertions.assertEquals(true, record.viaMobile)
        Assertions.assertEquals("public", record.visibility)
        Assertions.assertEquals(false, record.localOnly)
        Assertions.assertEquals(mutableListOf("uid2", "uid3"), record.visibleUserIds)
        Assertions.assertEquals("uri-uri-uri-uri", record.uri)
        Assertions.assertEquals("url-url-url-url", record.url)
        Assertions.assertEquals(9, record.renoteCount)
        Assertions.assertEquals(mutableMapOf(
            "like" to "1",
            "love" to "2",
            "haha" to "3",
            "wow" to "4",
            "sad" to "5",
            "angry" to "6",
        ), record.reactionCounts)
        Assertions.assertEquals(mutableMapOf(
            "name1" to "url1",
            "name2" to "url2",
            "name3" to "url3",
        ), record.emojis)
        Assertions.assertEquals(10, record.repliesCount)


    }

    @Test
    fun generateAccountAndNoteId() {
        val actual = NoteRecord.generateAccountAndNoteId(Note.Id(0L, "note0"))
        Assertions.assertEquals("0-note0", actual)
    }
}