package net.pantasystem.milktea.api.misskey.v12.channel

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.user.User

/**
 * APIから降ってきたJSONを直接的に変換するためのDTO
 * @param isFollowing followしているとtrue,未認証の場合はnullになる
 * @param hasUnreadNote 未読ノートが存在する場合trueになる,未認証の場合はnullになる
 */
@Serializable
data class ChannelDTO(
    val id: String,
    val createdAt: Instant,
    val lastNotedAt: Instant? = null,
    val name: String,
    val description: String? = null,
    val bannerUrl: String? = null,
    val notesCount: Int,
    val usersCount: Int,
    val userId: String?,
    val hasUnreadNote: Boolean? = null,
    val isFollowing: Boolean? = null,
) {
    fun toModel(account: Account): Channel {
        return Channel(
            id = Channel.Id(account.accountId, id),
            createdAt = createdAt,
            lastNotedAt = lastNotedAt,
            name = name,
            description = description,
            bannerUrl = bannerUrl,
            notesCount = notesCount,
            usersCount = usersCount,
            userId = userId?.let { User.Id(account.accountId, it) },
            hasUnreadNote = hasUnreadNote,
            isFollowing = isFollowing
        )
    }
}

@Serializable
data class ShowChannelDTO(
    val i: String,
    val channelId: String,
)

@Serializable
data class CreateChannelDTO(
    val i: String,
    val name: String,
    val description: String?,
    val bannerId: String?,
)

@Serializable
data class UpdateChannelDTO(
    val i: String,
    val name: String,
    val description: String?,
    val bannerId: String?,
)

@Serializable
data class FollowChannelDTO(
    val i: String,
    val channelId: String,
)

@Serializable
data class UnFollowChannelDTO(
    val i: String,
    val channelId: String,
)


@Serializable
data class FindPageable(
    val i: String?,
    val sinceId: String? = null,
    val untilId: String? = null,
    val limit: Int = 5
)

