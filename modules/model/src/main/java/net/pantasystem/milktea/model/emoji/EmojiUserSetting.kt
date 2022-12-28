package net.pantasystem.milktea.model.emoji

data class EmojiUserSetting(
    val reaction: String,
    val instanceDomain: String,
    var weight: Int
)
