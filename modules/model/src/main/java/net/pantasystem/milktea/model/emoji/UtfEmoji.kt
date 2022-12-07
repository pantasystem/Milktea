package net.pantasystem.milktea.model.emoji

import kotlinx.serialization.Serializable

@Serializable
data class Utf8Emoji(
    val codes: String,
    val name: String,
    val char: String,
)