package net.pantasystem.milktea.api.misskey.users.follow

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class FollowUserRequest(
    @SerialName("i") val i: String,
    @SerialName("userId") val userId: String,
)