package net.pantasystem.milktea.api.misskey.online.user

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class OnlineUserCount(
    @SerialName("count") val count: Int
)