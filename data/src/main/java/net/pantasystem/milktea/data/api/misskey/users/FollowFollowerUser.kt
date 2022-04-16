package net.pantasystem.milktea.data.api.misskey.users

import kotlinx.serialization.Serializable
import net.pantasystem.milktea.data.serializations.DateSerializer
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