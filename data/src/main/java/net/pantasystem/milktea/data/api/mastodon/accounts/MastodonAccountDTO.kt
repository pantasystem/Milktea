package net.pantasystem.milktea.data.api.mastodon.accounts

import net.pantasystem.milktea.data.api.mastodon.emojis.TootEmojiDTO
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.user.User

@Serializable
data class MastodonAccountDTO (
    val id: String,
    val username: String,
    val acct: String,

    @SerialName("display_name")
    val displayName: String,
    val locked: Boolean,
    val bot: Boolean,

    @SerialName("created_at")
    val createdAt: Instant,

    val note: String,
    val url: String,
    val avatar: String,

    @SerialName("avatar_static")
    val avatarStatic: String,
    val header: String,

    @SerialName("header_static")
    val headerStatic: String,

    val emojis: List<TootEmojiDTO>,

    @SerialName("followers_count")
    val followersCount: Int,

    @SerialName("following_count")
    val followingCount: Int,

    @SerialName("statuses_count")
    val statusesCount: Int,


) {
    fun toModel(account: net.pantasystem.milktea.model.account.Account): net.pantasystem.milktea.model.user.User {
        return net.pantasystem.milktea.model.user.User.Simple(
            net.pantasystem.milktea.model.user.User.Id(account.accountId, account.remoteId),
            userName = username,
            name = displayName,
            avatarUrl = avatar,
            emojis = emojis.map {
                net.pantasystem.milktea.model.emoji.Emoji(
                    name = it.shortcode,
                    uri = it.url,
                    url = it.url,
                    category = it.category,
                )
            },
            host = acct.split("@").getOrNull(1),
            isBot = bot,
            isCat = false,
            nickname = null,
        )
    }
}