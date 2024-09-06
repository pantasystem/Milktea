package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_emoji_aspects"
)
data class CustomEmojiAspectRatioEntity(
    @PrimaryKey(autoGenerate = false) val uri: String,
    @ColumnInfo("aspect_ratio") val aspectRatio: Float
)