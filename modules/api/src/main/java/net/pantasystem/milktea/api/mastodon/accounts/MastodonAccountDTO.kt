package net.pantasystem.milktea.api.mastodon.accounts

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.user.User

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
) {
    fun toModel(account: Account, related: User.Related? = null): User {
        return User.Detail(
            User.Id(account.accountId, this.id),
            userName = username,
            name = displayName,
            avatarUrl = avatar,
            emojis = emojis.map {
                CustomEmoji(
                    name = it.shortcode,
                    uri = it.url,
                    url = it.url,
                    category = it.category,
                )
            },
            host = acct.split("@").getOrNull(1) ?: account.getHost(),
            isBot = bot,
            isCat = false,
            nickname = null,
            isSameHost = acct.split("@").getOrNull(1) == null
                    || acct.split("@").getOrNull(1) == account.getHost(),
            instance = null,
            avatarBlurhash = null,
            info = User.Info(
                followersCount = followersCount.toInt(),
                followingCount = followingCount.toInt(),
                notesCount = statusesCount.toInt(),
                hostLower = null,
                pinnedNoteIds = null,
                bannerUrl = header,
                url = url,
                isLocked = locked,
                birthday = null,
                fields = emptyList(),
                createdAt = createdAt,
                updatedAt = null,
                isPublicReactions = false,
                description = note,
                ffVisibility = null,
            ),
            related = related,
            badgeRoles = emptyList(),
        )
    }
}


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