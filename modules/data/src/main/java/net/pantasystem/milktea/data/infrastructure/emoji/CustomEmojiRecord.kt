package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.emoji.CustomEmoji

@Entity(
    tableName = "custom_emojis",
    indices = [
        Index(value = ["emojiHost", "name"]),
        Index(value = ["emojiHost"]),
        Index(value = ["name"])
    ]
)
data class CustomEmojiRecord(
    val name: String,
    val emojiHost: String,
    val url: String? = null,
    val uri: String? = null,
    val type: String? = null,
    val serverId: String? = null,
    val category: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
) {

    companion object {
        fun from(model: CustomEmoji, host: String, id: Long = 0L): CustomEmojiRecord {
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


    fun toModel(aspectRatio: Float? = null, cachePath: String? = null): CustomEmoji {
        return CustomEmoji(
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
    ],
    indices = [
        Index(value = ["emojiId"]),
        Index(value = ["name"]),
    ],
    primaryKeys = ["name", "emojiId"],
)
data class CustomEmojiAliasRecord(
    val name: String,
    val emojiId: Long,
)