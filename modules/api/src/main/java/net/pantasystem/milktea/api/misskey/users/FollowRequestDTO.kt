package net.pantasystem.milktea.api.misskey.users

@kotlinx.serialization.Serializable
class FollowRequestDTO(
    val id: String,
    val follower: UserDTO,
    val followee: UserDTO
)