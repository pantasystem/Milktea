package net.pantasystem.milktea.data.infrastructure.note.impl.db

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.note.make
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.note.reaction.ReactionCount
import net.pantasystem.milktea.model.note.type
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

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
                ReactionCount("like", 1, me = true),
                ReactionCount("love", 2, me = false),
                ReactionCount("haha", 3, me = false),
                ReactionCount("wow", 4, me = true),
                ReactionCount("sad", 5, me = true),
                ReactionCount("angry", 6, me = true),
            ),
            emojis = listOf(
                CustomEmoji(name = "name1", url = "url1"),
                CustomEmoji(name = "name2", url = "url2"),
                CustomEmoji(name = "name3", url = "url3"),
            ),
            repliesCount = 10,
            fileIds = listOf(FileProperty.Id(0L, "fid1"), FileProperty.Id(0L, "fid2")),
            poll = null,
            myReaction = "like",
            channelId = null,
            maxReactionsPerAccount = 4
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
        Assertions.assertEquals(
            mutableMapOf(
                "like" to "1",
                "love" to "2",
                "haha" to "3",
                "wow" to "4",
                "sad" to "5",
                "angry" to "6",
            ), record.reactionCounts
        )
        Assertions.assertEquals(
            mutableMapOf(
                "name1" to "url1",
                "name2" to "url2",
                "name3" to "url3",
            ), record.emojis
        )
        Assertions.assertEquals(10, record.repliesCount)
        Assertions.assertEquals(listOf("like", "wow", "sad", "angry"), record.myReactions)
        Assertions.assertEquals(4, record.maxReactionsPerAccount)
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
        Assertions.assertEquals(
            mutableListOf(
                "choice1",
                "choice2",
                "choice3",
            ), record.pollChoices
        )
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
    fun applyModel_GiveMisskeyType() {
        val record = NoteRecord()
        val note = Note.make(
            Note.Id(0L, "nid1"),
            User.Id(0L, "uid1"),
            type = Note.Type.Misskey(
                channel = Note.Type.Misskey.SimpleChannelInfo(
                    id = Channel.Id(0L, "ch1"),
                    name = "name1",
                ),
                isAcceptingOnlyLikeReaction = false,
                isNotAcceptingSensitiveReaction = true,
                isRequireNyaize = true,
            )
        )
        record.applyModel(note)
        Assertions.assertEquals(
            "misskey",
            record.type
        )
        Assertions.assertEquals(
            "ch1",
            record.misskeyChannelId
        )
        Assertions.assertEquals(
            "name1",
            record.misskeyChannelName
        )
        Assertions.assertEquals(
            true,
            record.misskeyIsNotAcceptingSensitiveReaction
        )
        Assertions.assertEquals(
            true,
            record.misskeyIsRequireNyaize,
        )
    }

    @Test
    fun toModel_ReturnsNote() {
        val now = Clock.System.now()
        val record = NoteRecord(
            accountId = 1,
            noteId = "note-id",
            accountIdAndNoteId = "1-note-id",
            createdAt = now.toString(),
            text = "This is a test note",
            cw = "This is a CW",
            userId = "user1",
            replyId = "456",
            renoteId = "789",
            viaMobile = false,
            visibility = Visibility.Public(false).type(),
            localOnly = false,
            visibleUserIds = mutableListOf("visible-user-id-1", "visible-user-id-2"),
            url = "https://example.com/note/123",
            uri = "example://note/123",
            renoteCount = 10,
            reactionCounts = mutableMapOf("like" to "5", "smile" to "3"),
            emojis = mutableMapOf("smile" to "https://example.com/emoji/smile.png", "heart" to "https://example.com/emoji/heart.png"),
            repliesCount = 2,
            fileIds = mutableListOf("file-id-1", "file-id-2"),
            pollExpiresAt = (now + 60.seconds).toString(),
            pollMultiple = true,
            pollChoices = mutableListOf("Choice 1", "Choice 2", "Choice 3"),
            pollChoicesVotes = mutableListOf("2", "1", "0"),
            pollChoicesIsVoted = mutableListOf("false", "true", "false"),
            myReaction = "like",
            channelId = "channel-id",
            type = "mastodon",
            mastodonReblogged = true,
            mastodonFavourited = false,
            mastodonBookmarked = true,
            mastodonMuted = false,
            mastodonFavoriteCount = 3,
            mastodonTagNames = mutableListOf("tag1", "tag2"),
            mastodonTagUrls = mutableListOf("https://example.com/tag1", "https://example.com/tag2"),
            mastodonMentionIds = mutableListOf("mention-id-1", "mention-id-2"),
            mastodonMentionUserNames = mutableListOf("mention-username-1", "mention-username-2"),
            mastodonMentionUrls = mutableListOf("https://example.com/mention/1", "https://example.com/mention/2"),
            mastodonMentionAccts = mutableListOf("mention-acct-1", "mention-acct-2"),
            mastodonIsFedibirdQuote = true,
            mastodonPollId = "poll1",
            mastodonIsSensitive = false,
            mastodonPureText = "test note",
            mastodonIsReactionAvailable = true,
            myReactions = mutableListOf("like"),
            maxReactionsPerAccount = 3,
        )
        val expectedNote = Note(
            id = Note.Id(accountId = 1, noteId = "note-id"),
            createdAt = now,
            text = "This is a test note",
            cw = "This is a CW",
            userId = User.Id(accountId = 1, id = "user1"),
            replyId = Note.Id(accountId = 1, noteId = "456"),
            renoteId = Note.Id(accountId = 1, noteId = "789"),
            viaMobile = false,
            visibility = Visibility.Public(false),
            localOnly = false,
            visibleUserIds = listOf(
                User.Id(accountId = 1, id = "visible-user-id-1"),
                User.Id(accountId = 1, id = "visible-user-id-2")
            ),
            url = "https://example.com/note/123",
            uri = "example://note/123",
            renoteCount = 10,
            reactionCounts = listOf(
                ReactionCount(reaction = "like", count = 5, me = true),
                ReactionCount(reaction = "smile", count = 3, me = false)
            ),
            emojis = listOf(
                CustomEmoji(name = "smile", url = "https://example.com/emoji/smile.png"),
                CustomEmoji(name = "heart", url = "https://example.com/emoji/heart.png")
            ),
            repliesCount = 2,
            fileIds = listOf(
                FileProperty.Id(accountId = 1, fileId = "file-id-1"),
                FileProperty.Id(accountId = 1, fileId = "file-id-2")
            ),
            poll = Poll(
                expiresAt = now + 60.seconds,
                multiple = true,
                choices = listOf(
                    Poll.Choice(text = "Choice 1", votes = 2, isVoted = false, index = 0),
                    Poll.Choice(text = "Choice 2", votes = 1, isVoted = true, index = 1),
                    Poll.Choice(text = "Choice 3", votes = 0, isVoted = false, index = 2),
                )
            ),
            myReaction = "like",
            channelId = Channel.Id(accountId = 1, channelId = "channel-id"),
            type = Note.Type.Mastodon(
                reblogged = true,
                favorited = false,
                bookmarked = true,
                muted = false,
                favoriteCount = 3,
                tags = listOf(
                    Note.Type.Mastodon.Tag(name = "tag1", url = "https://example.com/tag1"),
                    Note.Type.Mastodon.Tag(name = "tag2", url = "https://example.com/tag2")
                ),
                mentions = listOf(
                    Note.Type.Mastodon.Mention(
                        id = "mention-id-1",
                        username = "mention-username-1",
                        acct = "mention-acct-1",
                        url = "https://example.com/mention/1"
                    ),
                    Note.Type.Mastodon.Mention(
                        id = "mention-id-2",
                        username = "mention-username-2",
                        acct = "mention-acct-2",
                        url = "https://example.com/mention/2"
                    )
                ),
                isFedibirdQuote = true,
                pollId = "poll1",
                isSensitive = false,
                pureText = "test note",
                isReactionAvailable = true,
            ),
            maxReactionsPerAccount = 3,
            emojiNameMap = emptyMap()
        )
        val actual =record.toModel()
        Assertions.assertEquals(expectedNote.id, actual.id)
        Assertions.assertEquals(expectedNote.createdAt, actual.createdAt)
        Assertions.assertEquals(expectedNote.text, actual.text)
        Assertions.assertEquals(expectedNote.cw, actual.cw)
        Assertions.assertEquals(expectedNote.userId, actual.userId)
        Assertions.assertEquals(expectedNote.replyId, actual.replyId)
        Assertions.assertEquals(expectedNote.renoteId, actual.renoteId)
        Assertions.assertEquals(expectedNote.viaMobile, actual.viaMobile)
        Assertions.assertEquals(expectedNote.visibility, actual.visibility)
        Assertions.assertEquals(expectedNote.localOnly, actual.localOnly)
        Assertions.assertEquals(expectedNote.visibleUserIds, actual.visibleUserIds)
        Assertions.assertEquals(expectedNote.url, actual.url)
        Assertions.assertEquals(expectedNote.uri, actual.uri)
        Assertions.assertEquals(expectedNote.renoteCount, actual.renoteCount)
        Assertions.assertEquals(expectedNote.reactionCounts, actual.reactionCounts)
        Assertions.assertEquals(expectedNote.emojis, actual.emojis)
        Assertions.assertEquals(expectedNote.repliesCount, actual.repliesCount)
        Assertions.assertEquals(expectedNote.fileIds, actual.fileIds)
        Assertions.assertEquals(expectedNote.poll, actual.poll)
        Assertions.assertEquals(expectedNote.myReaction, actual.myReaction)
        Assertions.assertEquals(expectedNote.channelId, actual.channelId)
        Assertions.assertEquals(expectedNote.type, actual.type)
        Assertions.assertEquals(expectedNote.maxReactionsPerAccount, actual.maxReactionsPerAccount)


    }

    @Test
    fun generateAccountAndNoteId() {
        val actual = NoteRecord.generateAccountAndNoteId(Note.Id(0L, "note0"))
        Assertions.assertEquals("0-note0", actual)
    }
}