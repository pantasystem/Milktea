package net.pantasystem.milktea.data.infrastructure.emoji.db

import androidx.room.*

@Entity(
    tableName = "custom_emojis",
    indices = [
        Index("name"),
        Index("host"),
        Index("category"),
        Index("host", "name", unique = true)
    ]
)
data class CustomEmojiRecord(
    val serverId: String? = null,
    val name: String,
    val host: String,
    val url: String? = null,
    val uri: String? = null,
    val type: String? = null,
    val category: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L
)

@Entity(
    tableName = "custom_emoji_aliases",
    indices = [
        Index("emojiId", "value")
    ],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            entity = CustomEmojiRecord::class,
            childColumns = ["emojiId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class CustomEmojiAliasRecord(
    val emojiId: Long,
    val value: String
)

data class CustomEmojiRelated(
    @Embedded val emoji: CustomEmojiRecord,

    @Relation(
        parentColumn = "id",
        entityColumn = "emojiId"
    )
    val aliases: List<CustomEmojiAliasRecord>
)