package net.pantasystem.milktea.api.misskey.v12.channel

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
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
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    val createdAt: Instant,

    @SerialName("lastNotedAt")
    val lastNotedAt: Instant? = null,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("bannerUrl")
    val bannerUrl: String? = null,

    @SerialName("notesCount")
    val notesCount: Int,

    @SerialName("usersCount")
    val usersCount: Int,

    @SerialName("userId")
    val userId: String?,

    @SerialName("hasUnreadNote")
    val hasUnreadNote: Boolean? = null,

    @SerialName("isFollowing")
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
    @SerialName("i")
    val i: String,

    @SerialName("channelId")
    val channelId: String,
)

@Serializable
data class CreateChannelDTO(
    @SerialName("i")
    val i: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?,

    @SerialName("bannerId")
    val bannerId: String?,
)

@Serializable
data class UpdateChannelDTO(
    @SerialName("i")
    val i: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?,

    @SerialName("bannerId")
    val bannerId: String?,
)

@Serializable
data class FollowChannelDTO(
    @SerialName("i")
    val i: String,

    @SerialName("channelId")
    val channelId: String,
)

@Serializable
data class UnFollowChannelDTO(
    @SerialName("i")
    val i: String,

    @SerialName("channelId")
    val channelId: String,
)


@Serializable
data class FindPageable(
    @SerialName("i")
    val i: String?,

    @SerialName("sinceId")
    val sinceId: String? = null,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("limit")
    val limit: Int = 5
)

