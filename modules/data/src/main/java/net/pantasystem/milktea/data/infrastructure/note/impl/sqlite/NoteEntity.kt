package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.note.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User

@Entity(
    tableName = "notes"
)
data class NoteEntity(
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "account_id")
    val accountId: Long,

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "text")
    val text: String?,

    @ColumnInfo(name = "cw")
    val cw: String?,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "reply_id")
    val replyId: String?,

    @ColumnInfo(name = "repost_id")
    val repostId: String?,

    @ColumnInfo(name = "via_mobile")
    val viaMobile: Boolean?,

    @ColumnInfo(name = "visibility")
    val visibility: String,

    @ColumnInfo(name = "local_only")
    val localOnly: Boolean?,

    @ColumnInfo(name = "url")
    val url: String?,

    @ColumnInfo(name = "uri")
    val uri: String?,

    @ColumnInfo(name = "repost_count")
    val repostCount: Int,

    @ColumnInfo(name = "reply_count")
    val replyCount: Int,

    @ColumnInfo(name = "channel_id")
    val channelId: String?,

    @ColumnInfo(name = "max_reaction_per_account")
    val maxReactionPerAccount: Int,


    // poll
    @ColumnInfo(name = "polls_expires_at")
    val pollExpiresAt: Instant?,

    @ColumnInfo(name = "polls_multiple")
    val pollMultiple: Boolean?,

    @ColumnInfo(name = "circle_id")
    val fedibirdCircleId: String?,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "mastodon_reblogged")
    val mastodonReblogged: Boolean?,

    @ColumnInfo(name = "mastodon_favourited")
    val mastodonFavourited: Boolean?,

    @ColumnInfo(name = "mastodon_bookmarked")
    val mastodonBookmarked: Boolean?,

    @ColumnInfo(name = "mastodon_muted")
    val mastodonMuted: Boolean?,

    @ColumnInfo(name = "mastodon_favourites_count")
    val mastodonFavouritesCount: Int?,

    @ColumnInfo(name = "mastodon_is_fedibird_quote")
    val mastodonIsFedibirdQuote: Boolean?,

    @ColumnInfo(name = "mastodon_poll_id")
    val mastodonPollId: String?,

    @ColumnInfo(name = "mastodon_is_sensitive")
    val mastodonIsSensitive: Boolean?,

    @ColumnInfo(name = "mastodon_pure_text")
    val mastodonPureText: String?,

    @ColumnInfo(name = "mastodon_is_reaction_available")
    val mastodonIsReactionAvailable: Boolean?,

    @ColumnInfo("misskey_channel_id")
    val misskeyChannelId: String?,

    @ColumnInfo("misskey_channel_name")
    val misskeyChannelName: String?,

    @ColumnInfo("misskey_is_accepting_only_like_reaction")
    val misskeyIsAcceptingOnlyLikeReaction: Boolean?,

    @ColumnInfo("misskey_is_not_accepting_sensitive_reaction")
    val misskeyIsNotAcceptingSensitiveReaction: Boolean?,

    @ColumnInfo("misskey_is_require_nyaize")
    val misskeyIsRequireNyaize: Boolean?,

    ) {

    companion object {
        fun makeEntityId(accountId: Long, noteId: String): String {
            return "$accountId:$noteId"
        }

        fun makeEntityId(noteId: Note.Id): String {
            return makeEntityId(noteId.accountId, noteId.noteId)
        }
    }


}

@Entity(
    tableName = "reaction_counts",
    primaryKeys = ["note_id", "reaction"]
)
data class ReactionCountEntity(
    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "reaction")
    val reaction: String,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "me")
    val me: Boolean,
)

@Entity(
    tableName = "note_visible_user_ids",
    primaryKeys = ["note_id", "user_id"]
)
data class NoteVisibleUserIdEntity(
    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,
)

@Entity(
    tableName = "note_poll_choices",
    primaryKeys = ["note_id", "index"]
)
data class NotePollChoiceEntity(
    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "index")
    val index: Int,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "votes")
    val votes: Int,

    @ColumnInfo(name = "is_voted")
    val isVoted: Boolean,
)

@Entity(
    tableName = "mastodon_tags",
    primaryKeys = ["note_id", "tag"]
)
data class MastodonTagEntity(
    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "tag")
    val tag: String,

    @ColumnInfo(name = "url")
    val url: String,
)

@Entity(
    tableName = "mastodon_mentions",
    primaryKeys = ["note_id", "user_id"]
)
data class MastodonMentionEntity(
    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "acct")
    val acct: String,

    @ColumnInfo(name = "url")
    val url: String,
)

@Entity(
    tableName = "note_custom_emojis",
)
data class NoteCustomEmojiEntity(
    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: Float,

    @ColumnInfo(name = "cache_path")
    val cachePath: String,
)

@Entity(
    tableName = "note_files",
    primaryKeys = ["note_id", "file_id"]
)
data class NoteFileEntity(
    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "file_id")
    val fileId: String,
)

data class NoteWithRelation(
    @Embedded
    val note: NoteEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    val reactionCounts: List<ReactionCountEntity>?,

    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    val visibleUserIds: List<NoteVisibleUserIdEntity>?,

    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    val pollChoices: List<NotePollChoiceEntity>?,

    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    val mastodonTags: List<MastodonTagEntity>?,

    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    val mastodonMentions: List<MastodonMentionEntity>?,

    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    val customEmojis: List<NoteCustomEmojiEntity>?,

    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    val noteFiles: List<NoteFileEntity>?,
) {
    fun toModel(): Note {
        val emojis = customEmojis?.map {
            CustomEmoji(
                name = it.name,
                url = it.url,
                aspectRatio = it.aspectRatio,
                cachePath = it.cachePath
            )
        }
        return Note(
            id = Note.Id(note.accountId, note.noteId),
            createdAt = note.createdAt,
            text = note.text,
            cw = note.cw,
            userId = User.Id(note.accountId, note.userId),
            replyId = note.replyId?.let { Note.Id(note.accountId, it) },
            renoteId = note.repostId?.let { Note.Id(note.accountId, it) },
            viaMobile = note.viaMobile,
            visibility = Visibility(note.visibility, note.fedibirdCircleId, note.localOnly),
            localOnly = note.localOnly,
            visibleUserIds = visibleUserIds?.map { User.Id(note.accountId, it.userId) },
            url = note.url,
            uri = note.uri,
            renoteCount = note.repostCount,
            reactionCounts = reactionCounts?.map {
                ReactionCount(it.reaction, it.count, it.me)
            } ?: emptyList(),
            emojis = emojis,
            repliesCount = note.replyCount,
            fileIds = noteFiles?.map { FileProperty.Id(note.accountId, it.fileId) },
            poll = if (pollChoices.isNullOrEmpty()) {
                null
            } else {
                Poll(
                    expiresAt = note.pollExpiresAt!!,
                    multiple = note.pollMultiple!!,
                    choices = pollChoices.map {
                        Poll.Choice(it.index, it.text, it.votes, it.isVoted)
                    }
                )
            },
            myReaction = reactionCounts?.firstOrNull { it.me }?.reaction,
            channelId = note.channelId?.let { Channel.Id(note.accountId, it) },
            type = when (note.type) {
                "mastodon" -> Note.Type.Mastodon(
                    reblogged = note.mastodonReblogged,
                    favorited = note.mastodonFavourited,
                    bookmarked = note.mastodonBookmarked,
                    muted = note.mastodonMuted,
                    favoriteCount = note.mastodonFavouritesCount,
                    tags = mastodonTags?.map {
                        Note.Type.Mastodon.Tag(it.tag, it.url)
                    } ?: emptyList(),
                    mentions = mastodonMentions?.map {
                        Note.Type.Mastodon.Mention(
                            it.userId,
                            it.username,
                            it.acct,
                            it.url
                        )
                    } ?: emptyList(),
                    isFedibirdQuote = note.mastodonIsFedibirdQuote ?: false,
                    pollId = note.mastodonPollId,
                    isSensitive = note.mastodonIsSensitive,
                    pureText = note.mastodonPureText,
                    isReactionAvailable = note.mastodonIsReactionAvailable ?: false,
                )
                "misskey" -> Note.Type.Misskey(
                    channel = note.channelId?.let {
                        Note.Type.Misskey.SimpleChannelInfo(
                            Channel.Id(note.accountId, it),
                            note.misskeyChannelName ?: "",
                        )
                    },
                    isAcceptingOnlyLikeReaction = note.misskeyIsAcceptingOnlyLikeReaction ?: false,
                    isNotAcceptingSensitiveReaction = note.misskeyIsNotAcceptingSensitiveReaction ?: false,
                    isRequireNyaize = note.misskeyIsRequireNyaize ?: false,
                )
                else -> Note.Type.Misskey()
            },
            maxReactionsPerAccount = note.maxReactionPerAccount,
            emojiNameMap = emojis?.associateBy { it.name },
        )
    }
}