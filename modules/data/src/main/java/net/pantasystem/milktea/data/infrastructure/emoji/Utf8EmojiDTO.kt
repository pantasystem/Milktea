package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "utf8_emojis_by_amio", primaryKeys = ["codes"])
data class Utf8EmojiDTO(
    @ColumnInfo(name = "codes") var codes: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "char") var charCode: String,
)
