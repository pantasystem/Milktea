package net.pantasystem.milktea.data.infrastructure.notes.impl.db

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.make
import net.pantasystem.milktea.model.notes.poll.Poll
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
    fun applyModel_GiveHasPollData() {
        val now = Clock.System.now()
        val record = NoteRecord()
        val note = Note.make(
            Note.Id(0L, "nid1"),
            User.Id(0L, "uid1"),
            poll = Poll(
                expiresAt = now,
                multiple = true,
                choices = listOf(
                    Poll.Choice(0, "choice1", 1, false),
                    Poll.Choice(1, "choice2", 2, true),
                    Poll.Choice(2, "choice3", 3, false),
                )
            )
        )
        record.applyModel(note)

        Assertions.assertEquals(now.toString(), record.pollExpiresAt)
        Assertions.assertEquals(true, record.pollMultiple)
        Assertions.assertEquals(mutableListOf(
            "choice1",
            "choice2",
            "choice3",
        ), record.pollChoices)
        Assertions.assertEquals(
            mutableListOf(
                "false",
                "true",
                "false"
            ),
            record.pollChoicesIsVoted,
        )
        Assertions.assertEquals(
            mutableListOf(
                "1",
                "2",
                "3"
            ),
            record.pollChoicesVotes,
        )
    }

    @Test
    fun applyModel_GiveMastodonType() {
        val record = NoteRecord()
        val note = Note.make(
            Note.Id(0L, "nid1"),
            User.Id(0L, "uid1"),
            type = Note.Type.Mastodon(
                reblogged = false,
                favorited = true,
                bookmarked = true,
                muted = false,
                favoriteCount = 100000,
                tags = listOf(
                    Note.Type.Mastodon.Tag(
                        name = "name1",
                        url = "url1",
                    ),
                    Note.Type.Mastodon.Tag(
                        name = "name2",
                        url = "url2",
                    ),

                ),
                mentions = listOf(
                    Note.Type.Mastodon.Mention(
                        id = "uid2",
                        username = "username2",
                        acct = "acct2",
                        url = "url2",
                    ),
                    Note.Type.Mastodon.Mention(
                        id = "uid3",
                        username = "username3",
                        acct = "acct3",
                        url = "url3",
                    ),
                    Note.Type.Mastodon.Mention(
                        id = "uid4",
                        username = "username4",
                        acct = "acct4",
                        url = "url4",
                    ),
                ),
                isFedibirdQuote = true,
                pollId = null,
                isSensitive = false,
                pureText = "pureText",
                isReactionAvailable = false,
            )
        )
        record.applyModel(note)

        Assertions.assertEquals(
            "mastodon",
            record.type
        )
        Assertions.assertEquals(
         false,
            record.mastodonReblogged
        )
        Assertions.assertEquals(
            true,
            record.mastodonFavourited
        )
        Assertions.assertEquals(
            true,
            record.mastodonBookmarked
        )
        Assertions.assertEquals(
            false,
            record.mastodonMuted
        )
        Assertions.assertEquals(
            100000,
            record.mastodonFavoriteCount
        )
        Assertions.assertEquals(
            mutableListOf(
                "name1",
                "name2",
            ),
            record.mastodonTagNames
        )
        Assertions.assertEquals(
            mutableListOf(
                "url1",
                "url2",
            ),
            record.mastodonTagUrls
        )
        Assertions.assertEquals(
            mutableListOf(
                "uid2",
                "uid3",
                "uid4",
            ),
            record.mastodonMentionIds
        )
        Assertions.assertEquals(
            mutableListOf(
                "username2",
                "username3",
                "username4",
            ),
            record.mastodonMentionUserNames
        )
        Assertions.assertEquals(
            mutableListOf(
                "acct2",
                "acct3",
                "acct4",
            ),
            record.mastodonMentionAccts
        )

        Assertions.assertEquals(
            mutableListOf(
                "url2",
                "url3",
                "url4",
            ),
            record.mastodonMentionUrls
        )
        Assertions.assertEquals(
            true,
            record.mastodonIsFedibirdQuote
        )
        Assertions.assertEquals(
            null,
            record.mastodonPollId
        )
        Assertions.assertEquals(
            false,
            record.mastodonIsSensitive
        )
        Assertions.assertEquals(
            "pureText",
            record.mastodonPureText
        )
        Assertions.assertEquals(
            false,
            record.mastodonIsReactionAvailable
        )

    }

    @Test
    fun generateAccountAndNoteId() {
        val actual = NoteRecord.generateAccountAndNoteId(Note.Id(0L, "note0"))
        Assertions.assertEquals("0-note0", actual)
    }
}