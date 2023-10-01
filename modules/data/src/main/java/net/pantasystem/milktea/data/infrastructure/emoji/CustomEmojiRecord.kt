package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.emoji.Emoji

@Entity(
    tableName = "custom_emojis"
)
data class CustomEmojiRecord(
    val name: String,
    val emojiHost: String,
    val url: String? = null,
    val uri: String? = null,
    val type: String? = null,
    val serverId: String? = null,
    val category: String? = null,
    @PrimaryKey val id: Long = 0L,
) {

    companion object {
        fun from(model: Emoji, host: String, id: Long = 0L): CustomEmojiRecord {
            return CustomEmojiRecord(
                name = model.name,
                emojiHost = host,
                url = model.url,
                uri = model.uri,
                type = model.type,
                serverId = model.id,
                category = model.category,
                id = id,
            )
        }
    }


    fun toModel(aspectRatio: Float? = null, cachePath: String? = null): Emoji {
        return Emoji(
            id = serverId,
            name = name,
            host = emojiHost,
            url = url,
            uri = uri,
            type = type,
            category = category,
            aspectRatio = aspectRatio,
            cachePath = cachePath,
        )
    }
}

@Entity(
    tableName = "custom_emoji_aliases",
    foreignKeys = [
        ForeignKey(
            entity = CustomEmojiRecord::class,
            parentColumns = ["id"],
            childColumns = ["emojiId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomEmojiAliasRecord(
    val name: String,
    @PrimaryKey val emojiId: Long,
)