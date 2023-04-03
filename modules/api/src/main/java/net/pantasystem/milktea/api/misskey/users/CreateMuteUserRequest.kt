package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CreateMuteUserRequest(
    @SerialName("i")
    val i: String,

    @SerialName("userId")
    val userId: String,

    @SerialName("expiresAt")
    val expiresAt: Long? = null,
)
