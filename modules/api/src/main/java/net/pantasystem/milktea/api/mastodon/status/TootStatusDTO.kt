package net.pantasystem.milktea.api.mastodon.status

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO
import net.pantasystem.milktea.api.mastodon.media.TootMediaAttachment
import net.pantasystem.milktea.api.mastodon.poll.TootPollDTO
import net.pantasystem.milktea.model.emoji.Emoji

@kotlinx.serialization.Serializable
data class TootStatusDTO(
    val id: String,
    val uri: String,
    @SerialName("created_at") val createdAt: Instant,
    val account: MastodonAccountDTO,
    val content: String,
    val visibility: String,
    val sensitive: Boolean,
    @SerialName("spoiler_text") val spoilerText: String,
    @SerialName("media_attachments") val mediaAttachments: List<TootMediaAttachment>,
    val mentions: List<Mention>? = null,
    val tags: List<Tag>? = null,
    val emojis: List<TootEmojiDTO>,
    @SerialName("reblogs_count") val reblogsCount: Int,
    @SerialName("favourites_count") val favouritesCount: Int? = null,
    @SerialName("replies_count") val repliesCount: Int,
    val url: String? = null,
    @SerialName("in_reply_to_id") val inReplyToId: String? = null,
    @SerialName("in_reply_account_id") val inReplyAccountId: String? = null,
    val reblog: TootStatusDTO? = null,
    val poll: TootPollDTO? = null,
    val card: TootPreviewCardDTO? = null,
    val language: String? = null,
    val text: String? = null,
    @SerialName("edited_at") val editedAt: Instant? = null,
    val favourited: Boolean? = null,
    val reblogged: Boolean? = null,
    val muted: Boolean? = null,
    val bookmarked: Boolean? = null,
    val pinned: Boolean? = null,
    val filtered: Boolean? = null,
    @SerialName("emoji_reactions") val emojiReactions: List<EmojiReactionCount>? = null
) {
    @kotlinx.serialization.Serializable
    data class Mention(
        val id: String,
        val username: String,
        val url: String,
        val acct: String,
    )

    @kotlinx.serialization.Serializable
    data class Tag(
        val name: String,
        val url: String,
    )


    @kotlinx.serialization.Serializable
    data class EmojiReactionCount(
        val name: String,
        val count: Int,
        @SerialName("account_ids") val accountIds: List<String>,
        val me: Boolean,

        val url: String? = null,
        val domain: String?  = null,
        @SerialName("static_url") val staticUrl: String? = null,
    ) {
        val isCustomEmoji = url != null || staticUrl != null

        val reaction = if (isCustomEmoji) {
            if (domain == null) {
                "$name@."
            } else {
                "$name@$domain"
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
                me
            }
        }
    }
}