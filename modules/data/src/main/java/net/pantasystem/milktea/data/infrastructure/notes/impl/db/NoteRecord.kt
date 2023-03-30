package net.pantasystem.milktea.data.infrastructure.notes.impl.db

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import kotlinx.datetime.toInstant
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.notes.type
import net.pantasystem.milktea.model.user.User

@Entity
data class NoteRecord(
    @Id
    var id: Long = 0,

    @Index
    var accountId: Long = 0,

    @Index
    var noteId: String = "",

    @Unique
    @Index
    var accountIdAndNoteId: String = "",

    var createdAt: String = "",

    var text: String? = null,
    var cw: String? = null,

    @Index
    var userId: String = "",
    var replyId: String? = null,
    var renoteId: String? = null,
    var viaMobile: Boolean? = null,
    var visibility: String = "",
    var fedibirdCircleId: String? = null,
    var localOnly: Boolean? = null,

    var visibleUserIds: MutableList<String>? = null,
    var url: String? = null,
    var uri: String? = null,
    var renoteCount: Int = 0,
    var reactionCounts: MutableMap<String, String> = mutableMapOf(),
    var emojis: MutableMap<String, String>? = null,
    var repliesCount: Int = 0,
    var fileIds: MutableList<String>? = null,

    var pollExpiresAt: String? = null,
    var pollMultiple: Boolean? = null,
    var pollChoices: MutableList<String>? = null,
    var pollChoicesVotes: MutableList<String>? = null,
    var pollChoicesIsVoted: MutableList<String>? = null,

    var myReaction: String? = null,
    var channelId: String? = null,

    var type: String = "",
    var mastodonReblogged: Boolean? = null,
    var mastodonFavourited: Boolean? = null,
    var mastodonBookmarked: Boolean? = null,
    var mastodonMuted: Boolean? = null,
    var mastodonFavoriteCount: Int? = null,

    var mastodonTagNames: MutableList<String>? = null,
    var mastodonTagUrls: MutableList<String>? = null,

    var mastodonMentionIds: MutableList<String>? = null,
    var mastodonMentionUserNames: MutableList<String>? = null,
    var mastodonMentionUrls: MutableList<String>? = null,
    var mastodonMentionAccts: MutableList<String>? = null,

    var mastodonIsFedibirdQuote: Boolean? = null,
    var mastodonPollId: String? = null,
    var mastodonIsSensitive: Boolean? = null,
    var mastodonPureText: String? = null,
    var mastodonIsReactionAvailable: Boolean? = null,

    var misskeyChannelId: String? = null,
    var misskeyChannelName: String? = null,
    var misskeyIsAcceptingOnlyLikeReaction: Boolean = false,

    var myReactions: MutableList<String>? = null,

) {

    companion object {
        fun from(model: Note): NoteRecord {
            val r = NoteRecord()
            r.applyModel(model)
            return r
        }

        fun generateAccountAndNoteId(noteId: Note.Id): String {
            return "${noteId.accountId}-${noteId.noteId}"
        }
    }


    fun applyModel(model: Note) {
        noteId = model.id.noteId
        accountId = model.id.accountId
        accountIdAndNoteId = generateAccountAndNoteId(model.id)
        createdAt = model.createdAt.toString()
        text = model.text
        cw = model.cw
        userId = model.userId.id
        replyId = model.replyId?.noteId
        renoteId = model.renoteId?.noteId
        viaMobile = model.viaMobile
        visibility = model.visibility.type()
        localOnly = model.localOnly
        visibleUserIds = model.visibleUserIds?.map { it.id }?.toMutableList()
        url = model.url
        uri = model.uri
        renoteCount = model.renoteCount
        reactionCounts =
            model.reactionCounts.associate { it.reaction to it.count.toString() }.toMutableMap()
        emojis = model.emojis?.associate { it.name to (it.url ?: it.uri ?: "") }?.toMutableMap()
        repliesCount = model.repliesCount
        fileIds = model.fileIds?.map { it.fileId }?.toMutableList()
        pollExpiresAt = model.poll?.expiresAt?.toString()
        pollMultiple = model.poll?.multiple
        pollChoices = model.poll?.choices?.map { it.text }?.toMutableList()
        pollChoicesVotes = model.poll?.choices?.map { it.votes.toString() }?.toMutableList()
        pollChoicesIsVoted = model.poll?.choices?.map { it.isVoted.toString() }?.toMutableList()
        myReaction = model.myReaction
        channelId = model.channelId?.channelId
        fedibirdCircleId = (model.visibility as? Visibility.Limited)?.circleId
        type = when (model.type) {
            is Note.Type.Mastodon -> "mastodon"
            is Note.Type.Misskey -> "misskey"
        }
        myReactions = model.reactionCounts.filter {
            it.me
        }.map {
            it.reaction
        }.toMutableList()
        when (val t = model.type) {
            is Note.Type.Mastodon -> {
                mastodonReblogged = t.reblogged
                mastodonFavourited = t.favorited
                mastodonBookmarked = t.bookmarked
                mastodonMuted = t.muted
                mastodonFavoriteCount = t.favoriteCount
                mastodonTagNames = t.tags.map { it.name }.toMutableList()
                mastodonTagUrls = t.tags.map { it.url }.toMutableList()
                mastodonMentionIds = t.mentions.map { it.id }.toMutableList()
                mastodonMentionUserNames = t.mentions.map { it.username }.toMutableList()
                mastodonMentionUrls = t.mentions.map { it.url }.toMutableList()
                mastodonMentionAccts = t.mentions.map { it.acct }.toMutableList()
                mastodonIsFedibirdQuote = t.isFedibirdQuote
                mastodonPollId = t.pollId
                mastodonIsSensitive = t.isSensitive
                mastodonPureText = t.pureText
                mastodonIsReactionAvailable = t.isReactionAvailable
            }
            is Note.Type.Misskey -> {
                misskeyChannelId = t.channel?.id?.channelId
                misskeyChannelName = t.channel?.name
                misskeyIsAcceptingOnlyLikeReaction = t.isAcceptingOnlyLikeReaction
            }
        }
    }

    fun toModel(): Note {

        return Note(
            id = Note.Id(accountId, noteId),
            createdAt = createdAt.toInstant(),
            text = text,
            cw = cw,
            userId = User.Id(accountId, userId),
            replyId = replyId?.let { Note.Id(accountId, it) },
            renoteId = renoteId?.let { Note.Id(accountId, it) },
            viaMobile = viaMobile,
            visibility = Visibility(visibility, fedibirdCircleId, localOnly),
            localOnly = localOnly,
            visibleUserIds = visibleUserIds?.map { User.Id(accountId, it) },
            url = url,
            uri = uri,
            renoteCount = renoteCount,
            reactionCounts = reactionCounts.map { entry ->
                ReactionCount(
                    entry.key,
                    entry.value.toInt(),
                    me = myReactions?.any {
                        it == entry.key
                    } ?: false
                ) },
            emojis = emojis?.map { Emoji(name = it.key, url = it.value) },
            repliesCount = repliesCount,
            fileIds = fileIds?.map { FileProperty.Id(accountId, it) },
            poll = getPoll(),
            myReaction = myReaction,
            channelId = channelId?.let { Channel.Id(accountId, it) },
            type = when (type) {
                "misskey" -> {
                    Note.Type.Misskey(
                        channel = misskeyChannelId?.let {
                            Note.Type.Misskey.SimpleChannelInfo(
                                id = Channel.Id(accountId, it),
                                name = misskeyChannelName ?: ""
                            )
                        },
                        isAcceptingOnlyLikeReaction = misskeyIsAcceptingOnlyLikeReaction
                    )
                }
                "mastodon" -> {
                    Note.Type.Mastodon(
                        reblogged = mastodonReblogged,
                        favorited = mastodonFavourited,
                        bookmarked = mastodonBookmarked,
                        muted = mastodonMuted,
                        favoriteCount = mastodonFavoriteCount,
                        tags = getMastodonTags() ?: emptyList(),
                        mentions = getMastodonMentions() ?: emptyList(),
                        isFedibirdQuote = mastodonIsFedibirdQuote ?: false,
                        pollId = mastodonPollId,
                        isSensitive = mastodonIsSensitive ?: false,
                        pureText = mastodonPureText,
                        isReactionAvailable = mastodonIsReactionAvailable ?: false
                    )
                }
                else -> throw IllegalArgumentException()
            },
            app = null,
        )
    }

    private fun getPoll(): Poll? {
        if (pollChoices.isNullOrEmpty()) {
            return null
        }
        if (pollChoicesVotes.isNullOrEmpty()) {
            return null
        }
        if (pollChoicesIsVoted.isNullOrEmpty()) {
            return null
        }

        if (pollChoices!!.size != pollChoicesVotes!!.size || pollChoices!!.size != pollChoicesIsVoted!!.size) {
            return null
        }

        return Poll(
            expiresAt = pollExpiresAt?.toInstant(),
            multiple = pollMultiple ?: false,
            choices = pollChoices!!.mapIndexed { index, text ->
                Poll.Choice(
                    text = text,
                    votes = pollChoicesVotes!![index].toInt(),
                    isVoted = pollChoicesIsVoted!![index].toBoolean(),
                    index = index
                )
            }
        )
    }

    private fun getMastodonTags(): List<Note.Type.Mastodon.Tag>? {
        if (mastodonTagNames.isNullOrEmpty() || mastodonTagUrls.isNullOrEmpty()) {
            return null
        }
        if (mastodonTagNames?.size != mastodonTagUrls?.size) {
            return null
        }

        return mastodonTagNames!!.mapIndexed { index, name ->
            Note.Type.Mastodon.Tag(
                name = name,
                url = mastodonTagUrls!![index]
            )
        }
    }

    private fun getMastodonMentions(): List<Note.Type.Mastodon.Mention>? {
        if (mastodonMentionAccts.isNullOrEmpty() || mastodonMentionIds.isNullOrEmpty() || mastodonMentionUserNames.isNullOrEmpty() || mastodonMentionUrls.isNullOrEmpty()) {
            return null
        }

        if (mastodonMentionAccts?.size != mastodonMentionIds?.size || mastodonMentionAccts?.size != mastodonMentionUserNames?.size || mastodonMentionAccts?.size != mastodonMentionUrls?.size) {
            return null
        }

        return mastodonMentionAccts!!.mapIndexed { index, acct ->
            Note.Type.Mastodon.Mention(
                id = mastodonMentionIds!![index],
                username = mastodonMentionUserNames!![index],
                acct = acct,
                url = mastodonMentionUrls!![index]
            )
        }
    }
}
