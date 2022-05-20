package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.room.ColumnInfo
import androidx.room.Entity
import net.pantasystem.milktea.model.emoji.Utf8Emoji

@Entity(tableName = "utf8_emojis_by_amio", primaryKeys = ["codes"])
data class Utf8EmojiDTO(
    @ColumnInfo(name = "codes") var codes: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "char") var charCode: String,
)

fun Utf8EmojiDTO.toModel(): Utf8Emoji {
    return Utf8Emoji(codes, name, charCode)
}

fun Utf8Emoji.toDTO(): Utf8EmojiDTO {
    return Utf8EmojiDTO(codes, name, char)
}