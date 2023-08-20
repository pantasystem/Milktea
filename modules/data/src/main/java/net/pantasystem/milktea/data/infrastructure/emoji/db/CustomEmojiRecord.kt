package net.pantasystem.milktea.data.infrastructure.emoji.db

//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.Index
//import androidx.room.PrimaryKey
//
//@Entity(
//    tableName = "custom_emojis",
//    indices = [
//        Index("name"),
//        Index("emojiHost"),
//        Index("category"),
//        Index("emojiHost", "name", unique = true)
//    ]
//)
//data class CustomEmojiRecord(
//    @ColumnInfo(name = "serverId")
//    val serverId: String? = null,
//
//    @ColumnInfo(name = "name")
//    val name: String,
//
//    @ColumnInfo(name = "emojiHost")
//    val emojiHost: String,
//
//    @ColumnInfo(name = "url")
//    val url: String? = null,
//
//    @ColumnInfo(name = "uri")
//    val uri: String? = null,
//
//    @ColumnInfo(name = "type")
//    val type: String? = null,
//
//    @ColumnInfo(name = "category")
//    val category: String? = null,
//
//    @ColumnInfo(name = "id")
//    @PrimaryKey(autoGenerate = true) val id: Long = 0L
//)

//@Entity(
//    tableName = "custom_emoji_aliases",
//    indices = [
//        Index("emojiId", "value")
//    ],
//    primaryKeys = ["emojiId", "value"],
//    foreignKeys = [
//        ForeignKey(
//            parentColumns = ["id"],
//            entity = CustomEmojiRecord::class,
//            childColumns = ["emojiId"],
//            onUpdate = ForeignKey.CASCADE,
//            onDelete = ForeignKey.CASCADE,
//        )
//    ]
//)
//data class CustomEmojiAliasRecord(
//    @ColumnInfo(name = "emojiId")
//    val emojiId: Long,
//
//    @ColumnInfo(name = "value")
//    val value: String
//)
