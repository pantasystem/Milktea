package net.pantasystem.milktea.api.misskey.v10

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.users.UserDTO

@Serializable
data class FollowFollowerUsers(
    @SerialName("users")
    val users: List<UserDTO>,

    @SerialName("next")
    val next: String? = null,
)