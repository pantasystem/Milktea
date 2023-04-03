package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcceptFollowRequest(
    @SerialName("i")
    val i: String,

    @SerialName("userId")
    val userId: String,
)