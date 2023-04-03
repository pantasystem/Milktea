package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
class FollowRequestDTO(
    @SerialName("id")
    val id: String,

    @SerialName("follower")
    val follower: UserDTO,

    @SerialName("followee")
    val followee: UserDTO
)