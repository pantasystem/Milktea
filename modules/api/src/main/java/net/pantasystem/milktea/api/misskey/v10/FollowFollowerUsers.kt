package net.pantasystem.milktea.api.misskey.v10

import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.users.UserDTO

@Serializable
data class FollowFollowerUsers(
    val users: List<UserDTO>,
    val next: String? = null,
)