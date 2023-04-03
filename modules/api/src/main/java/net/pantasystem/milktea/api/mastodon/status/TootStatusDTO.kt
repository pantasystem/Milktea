package net.pantasystem.milktea.api.mastodon.status

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO
import net.pantasystem.milktea.api.mastodon.filter.FilterResultDTO
import net.pantasystem.milktea.api.mastodon.media.TootMediaAttachment
import net.pantasystem.milktea.api.mastodon.poll.TootPollDTO
import net.pantasystem.milktea.common.serializations.EnumIgnoreUnknownSerializer
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note

@kotlinx.serialization.Serializable
data class TootStatusDTO(
    @SerialName("id")
    val id: String,

    @SerialName("uri")
    val uri: String,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("account")
    val account: MastodonAccountDTO,

    @SerialName("content")
    val content: String? = null,

    @SerialName("visibility")
    val visibility: StatusVisibilityType,

    @SerialName("sensitive")
    val sensitive: Boolean,

    @SerialName("spoiler_text")
    val spoilerText: String,

    @SerialName("media_attachments")
    val mediaAttachments: List<TootMediaAttachment>,

    @SerialName("mentions")
    val mentions: List<Mention>? = null,

    @SerialName("tags")
    val tags: List<Tag>? = null,

    @SerialName("emojis")
    val emojis: List<TootEmojiDTO>,

    @SerialName("reblogs_count")
    val reblogsCount: Int,

    @SerialName("favourites_count")
    val favouritesCount: Int? = null,

    @SerialName("replies_count")
    val repliesCount: Int,

    @SerialName("url")
    val url: String? = null,

    @SerialName("in_reply_to_id")
    val inReplyToId: String? = null,

    @SerialName("in_reply_account_id")
    val inReplyAccountId: String? = null,

    @SerialName("reblog")
    val reblog: TootStatusDTO? = null,

    @SerialName("poll")
    val poll: TootPollDTO? = null,

    @SerialName("card")
    val card: TootPreviewCardDTO? = null,

    @SerialName("language")
    val language: String? = null,

    @SerialName("text")
    val text: String? = null,

    @SerialName("edited_at")
    val editedAt: Instant? = null,

    @SerialName("favourited")
    val favourited: Boolean? = null,

    @SerialName("reblogged")
    val reblogged: Boolean? = null,

    @SerialName("muted")
    val muted: Boolean? = null,

    @SerialName("bookmarked")
    val bookmarked: Boolean? = null,

    @SerialName("pinned")
    val pinned: Boolean? = null,

    @SerialName("filtered")
    val filtered: List<FilterResultDTO>? = null,

    @SerialName("emoji_reactions")
    val emojiReactions: List<EmojiReactionCount>? = null,

    @SerialName("quote")
    val quote: TootStatusDTO? = null,

    @SerialName("circle_id")
    val circleId: String? = null,

    @SerialName("visibility_ex")
    val visibilityEx: String? = null,
) {
    @kotlinx.serialization.Serializable
    data class Mention(
        @SerialName("id")
        val id: String,

        @SerialName("username")
        val username: String,

        @SerialName("url")
        val url: String,

        @SerialName("acct")
        val acct: String,
    ) {
        fun toModel(): Note.Type.Mastodon.Mention {
            return Note.Type.Mastodon.Mention(
                id = id,
                username = username,
                url = url,
                acct = acct
            )
        }
    }

    @kotlinx.serialization.Serializable
    data class Tag(
        @SerialName("name")
        val name: String,

        @SerialName("url")
        val url: String,
    ) {
        fun toModel(): Note.Type.Mastodon.Tag {
            return Note.Type.Mastodon.Tag(
                name = name,
                url = url,
            )
        }
    }


    @kotlinx.serialization.Serializable
    data class EmojiReactionCount(
        @SerialName("name")
        val name: String,

        @SerialName("count")
        val count: Int,

        @SerialName("account_ids")
        val accountIds: List<String>,

        @SerialName("me")
        val me: Boolean? = null,

        @SerialName("url")
        val url: String? = null,

        @SerialName("domain")
        val domain: String?  = null,

        @SerialName("static_url")
        val staticUrl: String? = null,
    ) {
        val isCustomEmoji = url != null || staticUrl != null

        val reaction = if (isCustomEmoji) {
            if (domain == null) {
                ":$name@.:"
            } else {
                ":$name@$domain:"
            }
        } else {
            name
        }
        
        fun getEmoji(): Emoji? {
            if (!isCustomEmoji) {
                return null
            }
            return Emoji(
                name = if (domain == null) {
                    "$name@."
                } else {
                    "$name@$domain"
                },
                url = url,
                host = domain,
            )
        }

        fun myReaction(): String? {
            return reaction.takeIf {
                me == true
            }
        }
    }
}

object TootStatusVisibilityTypeSerializer : EnumIgnoreUnknownSerializer<StatusVisibilityType>(StatusVisibilityType.values(), StatusVisibilityType.Public)

@kotlinx.serialization.Serializable(with = TootStatusVisibilityTypeSerializer::class)
enum class StatusVisibilityType {
    @SerialName("private") Private,
    @SerialName("unlisted") Unlisted,
    @SerialName("public") Public,
    @SerialName("direct") Direct
}