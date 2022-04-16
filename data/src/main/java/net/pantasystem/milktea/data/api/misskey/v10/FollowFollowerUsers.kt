package net.pantasystem.milktea.data.api.misskey.v10

import net.pantasystem.milktea.data.api.misskey.users.UserDTO

data class FollowFollowerUsers(
    val users: List<UserDTO>,
    val next: String
)