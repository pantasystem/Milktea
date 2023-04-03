package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.users.UserDTO

@Serializable
data class AccessToken(
    @SerialName("accessToken")
    val accessToken: String,

    @SerialName("user")
    val user: UserDTO,
)

