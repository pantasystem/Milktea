package net.pantasystem.milktea.api.mastodon.accounts

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO

@Serializable
data class MastodonAccountDTO(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

    @SerialName("acct")
    val acct: String,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("locked")
    val locked: Boolean,

    @SerialName("bot")
    val bot: Boolean,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("note")
    val note: String,

    @SerialName("url")
    val url: String,

    @SerialName("avatar")
    val avatar: String,

    @SerialName("avatar_static")
    val avatarStatic: String,

    @SerialName("header")
    val header: String,

    @SerialName("header_static")
    val headerStatic: String,

    @SerialName("emojis")
    val emojis: List<TootEmojiDTO>,

    @SerialName("followers_count")
    val followersCount: Long,

    @SerialName("following_count")
    val followingCount: Long,

    @SerialName("statuses_count")
    val statusesCount: Long,
)


@Serializable
data class MastodonAccountRelationshipDTO(
    @SerialName("id")
    val id: String,

    @SerialName("following")
    val following: Boolean,

    @SerialName("showing_reblogs")
    val showingReblogs: Boolean? = null,

    @SerialName("notifying")
    val notifying: Boolean? = null,


    @SerialName("followed_by")
    val followedBy: Boolean,

    @SerialName("blocking")
    val blocking: Boolean,

    @SerialName("blocked_by")
    val blockedBy: Boolean,

    @SerialName("muting")
    val muting: Boolean,

    @SerialName("muting_notifications")
    val mutingNotifications: Boolean,

    @SerialName("requested")
    val requested: Boolean,

    @SerialName("domain_blocking")
    val domainBlocking: Boolean,

    @SerialName("endorsed")
    val endorsed: Boolean,

    @SerialName("note")
    val note: String,
)