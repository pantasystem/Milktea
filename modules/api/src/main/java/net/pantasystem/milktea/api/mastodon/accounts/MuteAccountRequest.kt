package net.pantasystem.milktea.api.mastodon.accounts

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class MuteAccountRequest(
    @SerialName("duration")
    val duration: Long = 0L,

    @SerialName("notifications")
    val notifications: Boolean = true
)