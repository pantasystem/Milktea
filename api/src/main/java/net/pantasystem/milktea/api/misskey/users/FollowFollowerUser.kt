package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.common.serializations.DateSerializer
import java.util.*

@Serializable
data class FollowFollowerUser @OptIn(ExperimentalSerializationApi::class) constructor(
    val id: String,
    @Serializable(with = DateSerializer::class) val createdAt: Date,
    val followeeId: String? = null,
    val followee: UserDTO? = null,
    val followerId: String? = null,
    val follower: UserDTO? = null
)