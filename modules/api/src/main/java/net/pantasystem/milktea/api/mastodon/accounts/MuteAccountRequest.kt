package net.pantasystem.milktea.api.mastodon.accounts

data class MuteAccountRequest(
    val duration: Long = 0L,
    val notifications: Boolean = true
)