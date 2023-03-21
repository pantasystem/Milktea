package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RejectFollowRequest(
    @SerialName("i")
    val i: String,

    @SerialName("userId")
    val userId: String,
)