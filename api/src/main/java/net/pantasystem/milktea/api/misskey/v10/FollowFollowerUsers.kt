package net.pantasystem.milktea.api.misskey.v10

import net.pantasystem.milktea.api.misskey.users.UserDTO


data class FollowFollowerUsers(
    val users: List<UserDTO>,
    val next: String
)