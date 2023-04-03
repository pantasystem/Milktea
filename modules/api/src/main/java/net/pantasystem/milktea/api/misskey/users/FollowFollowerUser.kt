package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.common.serializations.DateSerializer
import java.util.*

@Serializable
data class FollowFollowerUser @OptIn(ExperimentalSerializationApi::class) constructor(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    @Serializable(with = DateSerializer::class) val createdAt: Date,

    @SerialName("followeeId")
    val followeeId: String? = null,

    @SerialName("followee")
    val followee: UserDTO? = null,

    @SerialName("followerId")
    val followerId: String? = null,

    @SerialName("follower")
    val follower: UserDTO? = null
)