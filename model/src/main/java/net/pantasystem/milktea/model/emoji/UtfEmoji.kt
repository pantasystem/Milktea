package net.pantasystem.milktea.model.emoji

import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class Utf8Emoji(
    @PrimaryKey val codes: String,
    val name: String,
    val char: String,
)