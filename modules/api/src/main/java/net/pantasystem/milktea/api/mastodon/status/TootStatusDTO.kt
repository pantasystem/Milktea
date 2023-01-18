package net.pantasystem.milktea.api.mastodon.status

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO
import net.pantasystem.milktea.api.mastodon.media.TootMediaAttachment
import net.pantasystem.milktea.api.mastodon.poll.TootPollDTO

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
    val mentions: List<Mention>?,
    val tags: List<Tag>?,
    val emojis: List<TootEmojiDTO>,
    @SerialName("reblogs_count") val reblogsCount: Int,
    @SerialName("favorites_count") val favoritesCount: Int,
    @SerialName("replies_count") val repliesCount: Int,
    val url: String,
    @SerialName("in_reply_to_id") val inReplyToId: String?,
    @SerialName("in_reply_account_id") val inReplyAccountId: String?,
    val reblog: TootStatusDTO?,
    val poll: TootPollDTO?,
    val card: TootPreviewCardDTO?,
    val language: String?,
    val text: String?,
    @SerialName("edited_at") val editedAt: Instant?,
    val favorited: Boolean?,
    val reblogged: Boolean?,
    val muted: Boolean?,
    val bookmarked: Boolean?,
    val pinned: Boolean?,
    val filtered: Boolean?,

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
}