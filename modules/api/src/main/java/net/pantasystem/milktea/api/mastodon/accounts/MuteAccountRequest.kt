package net.pantasystem.milktea.api.mastodon.accounts

@kotlinx.serialization.Serializable
data class MuteAccountRequest(
    val duration: Long = 0L,
    val notifications: Boolean = true
)