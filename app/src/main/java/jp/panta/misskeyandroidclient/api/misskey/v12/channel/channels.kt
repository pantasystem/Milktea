package jp.panta.misskeyandroidclient.api.misskey.v12.channel

import jp.panta.misskeyandroidclient.model.channel.Channel
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.datetime.Instant

data class ChannelDTO(
    val id: Channel.Id,
    val createdAt: Instant,
    val lastNotedAt: Instant?,
    val name: String,
    val description: String?,
    val bannerUrl: String?,
    val notesCount: Int,
    val usersCount: Int,
    val userId: User.Id?,
)

data class ShowChannelDTO(
    val i: String,
    val channelId: String,
)

data class CreateChannelDTO(
    val i: String,
    val name: String,
    val description: String?,
    val bannerId: String?,
)

data class UpdateChannelDTO(
    val i: String,
    val name: String,
    val description: String?,
    val bannerId: String?,
)

data class FollowChannelDTO(
    val i: String,
    val channelId: String,
)

data class UnFollowChannelDTO(
    val i: String,
    val channelId: String,
)


data class FindPageable(
    val i: String?,
    val sinceId: String?,
    val untilId: String?,
    val limit: Int = 5
)

