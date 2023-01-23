package net.pantasystem.milktea.model.emoji

data class UserEmojiConfig(
    val reaction: String,
    val instanceDomain: String,
    var weight: Int
)
