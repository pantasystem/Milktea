package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.note.reaction.ReactionCount
import net.pantasystem.milktea.model.note.type
import net.pantasystem.milktea.model.user.User

@Entity(
    tableName = "notes",
)
data class NoteEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = false) val id: String,

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

        fun fromModel(model: Note): NoteEntity {
            return NoteEntity(
                id = makeEntityId(model.id),
                accountId = model.id.accountId,
                noteId = model.id.noteId,
                createdAt = model.createdAt,
                text = model.text,
                cw = model.cw,
                userId = model.userId.id,
                replyId = model.replyId?.noteId,
                repostId = model.renoteId?.noteId,
                viaMobile = model.viaMobile,
                visibility = model.visibility.type(),
                localOnly = model.localOnly,
                url = model.url,
                uri = model.uri,
                repostCount = model.renoteCount,
                replyCount = model.repliesCount,
                channelId = model.channelId?.channelId,
                maxReactionPerAccount = model.maxReactionsPerAccount,
                pollExpiresAt = model.poll?.expiresAt,
                pollMultiple = model.poll?.multiple,
                fedibirdCircleId = (model.visibility as? Visibility.Limited)?.circleId,
                type = when (model.type) {
                    is Note.Type.Mastodon -> "mastodon"
                    is Note.Type.Misskey -> "misskey"
                },
                mastodonReblogged = (model.type as? Note.Type.Mastodon)?.reblogged,
                mastodonFavourited = (model.type as? Note.Type.Mastodon)?.favorited,
                mastodonBookmarked = (model.type as? Note.Type.Mastodon)?.bookmarked,
                mastodonMuted = (model.type as? Note.Type.Mastodon)?.muted,
                mastodonFavouritesCount = (model.type as? Note.Type.Mastodon)?.favoriteCount,
                mastodonIsFedibirdQuote = (model.type as? Note.Type.Mastodon)?.isFedibirdQuote,
                mastodonPollId = (model.type as? Note.Type.Mastodon)?.pollId,
                mastodonIsSensitive = (model.type as? Note.Type.Mastodon)?.isSensitive,
                mastodonPureText = (model.type as? Note.Type.Mastodon)?.pureText,
                mastodonIsReactionAvailable = (model.type as? Note.Type.Mastodon)?.isReactionAvailable,
                misskeyChannelId = (model.type as? Note.Type.Misskey)?.channel?.id?.channelId,
                misskeyChannelName = (model.type as? Note.Type.Misskey)?.channel?.name,
                misskeyIsAcceptingOnlyLikeReaction = (model.type as? Note.Type.Misskey)?.isAcceptingOnlyLikeReaction,
                misskeyIsNotAcceptingSensitiveReaction = (model.type as? Note.Type.Misskey)?.isNotAcceptingSensitiveReaction,
                misskeyIsRequireNyaize = (model.type as? Note.Type.Misskey)?.isRequireNyaize,
            )
        }
    }


}

@Entity(
    tableName = "reaction_counts",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("note_id")
    ]
)
data class ReactionCountEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "reaction")
    val reaction: String,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "me")
    val me: Boolean,

    @ColumnInfo(name = "weight")
    val weight: Int?,
) {
    companion object {
        fun makeId(noteId: String, reaction: String): String {
            return "$noteId:$reaction"
        }
    }
}

@Entity(
    tableName = "note_visible_user_ids",
    primaryKeys = ["note_id", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("note_id")
    ],
)
data class NoteVisibleUserIdEntity(
    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,
)

@Entity(
    tableName = "note_poll_choices",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("note_id")
    ]
)
data class NotePollChoiceEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,

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
) {
    companion object {
        fun makeId(noteId: String, index: Int): String {
            return "$noteId:$index"
        }
    }
}

@Entity(
    tableName = "mastodon_tags",
    primaryKeys = ["note_id", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("note_id")
    ]
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
    primaryKeys = ["note_id", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("note_id")
    ]
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
) {
    companion object {
        fun fromModel(model: Note.Type.Mastodon.Mention, noteId: String): MastodonMentionEntity {
            return MastodonMentionEntity(
                noteId = noteId,
                userId = model.id,
                username = model.username,
                acct = model.acct,
                url = model.url,
            )
        }
    }
}

@Entity(
    tableName = "note_custom_emojis",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("note_id")
    ]
)
data class NoteCustomEmojiEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = false)
    val id: String,

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "url")
    val url: String?,

    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: Float?,

    @ColumnInfo(name = "cache_path")
    val cachePath: String?,
) {
    companion object {
        fun makeId(noteId: String, name: String): String {
            return "$noteId:$name"
        }

        fun fromModel(model: CustomEmoji, noteId: String): NoteCustomEmojiEntity {
            return NoteCustomEmojiEntity(
                id = makeId(noteId, model.name),
                noteId = noteId,
                name = model.name,
                url = model.url,
                aspectRatio = model.aspectRatio,
                cachePath = model.cachePath,
            )
        }
    }

    fun equalModel(
        customEmoji: CustomEmoji
    ): Boolean {
        return (name == customEmoji.name
                && url == (customEmoji.url ?: customEmoji.uri)
                && aspectRatio == customEmoji.aspectRatio
                && cachePath == customEmoji.cachePath)
    }
}

@Entity(
    tableName = "note_files",
    primaryKeys = ["note_id", "file_id"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("note_id")
    ],
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
            reactionCounts = reactionCounts?.sortedBy {
                it.weight
            }?.map {
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
                    }.sortedBy { it.index }
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
                    isNotAcceptingSensitiveReaction = note.misskeyIsNotAcceptingSensitiveReaction
                        ?: false,
                    isRequireNyaize = note.misskeyIsRequireNyaize ?: false,
                )

                else -> Note.Type.Misskey()
            },
            maxReactionsPerAccount = note.maxReactionPerAccount,
            emojiNameMap = emojis?.associateBy { it.name },
        )
    }

    companion object {
        fun fromModel(model: Note): NoteWithRelation {
            return NoteWithRelation(
                note = NoteEntity.fromModel(model),
                reactionCounts = model.reactionCounts.mapIndexed { index, reactionCount ->
                    ReactionCountEntity(
                        id = ReactionCountEntity.makeId(
                            NoteEntity.makeEntityId(model.id),
                            reactionCount.reaction
                        ),
                        noteId = NoteEntity.makeEntityId(model.id),
                        reaction = reactionCount.reaction,
                        count = reactionCount.count,
                        me = reactionCount.me,
                        weight = index
                    )
                },
                visibleUserIds = model.visibleUserIds?.map {
                    NoteVisibleUserIdEntity(
                        noteId = NoteEntity.makeEntityId(model.id),
                        userId = it.id,
                    )
                },
                pollChoices = model.poll?.choices?.map {
                    NotePollChoiceEntity(
                        noteId = NoteEntity.makeEntityId(model.id),
                        index = it.index,
                        text = it.text,
                        votes = it.votes,
                        isVoted = it.isVoted,
                        id = NotePollChoiceEntity.makeId(
                            NoteEntity.makeEntityId(model.id),
                            it.index
                        )
                    )
                },
                mastodonTags = (model.type as? Note.Type.Mastodon)?.tags?.map {
                    MastodonTagEntity(
                        noteId = NoteEntity.makeEntityId(model.id),
                        tag = it.name,
                        url = it.url,
                    )
                },
                mastodonMentions = (model.type as? Note.Type.Mastodon)?.mentions?.map {
                    MastodonMentionEntity(
                        noteId = NoteEntity.makeEntityId(model.id),
                        userId = it.id,
                        username = it.username,
                        acct = it.acct,
                        url = it.url,
                    )
                },
                customEmojis = model.emojis?.map {
                    NoteCustomEmojiEntity.fromModel(it, NoteEntity.makeEntityId(model.id))
                },
                noteFiles = model.fileIds?.map {
                    NoteFileEntity(
                        noteId = NoteEntity.makeEntityId(model.id),
                        fileId = it.fileId,
                    )
                },
            )

        }
    }
}