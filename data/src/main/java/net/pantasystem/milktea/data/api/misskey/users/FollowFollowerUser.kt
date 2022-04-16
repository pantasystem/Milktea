package net.pantasystem.milktea.data.api.misskey.users

import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class FollowFollowerUser(
    val id: String,
    @Serializable(with = DateSerializer::class) val createdAt: Date,
    val followeeId: String,
    val followee: UserDTO?,
    val followerId: String,
    val follower: UserDTO?
)