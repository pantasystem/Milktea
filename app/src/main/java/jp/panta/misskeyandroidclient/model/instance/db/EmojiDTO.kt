package jp.panta.misskeyandroidclient.model.instance.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.instance.Meta

@Entity(
    tableName = "emoji_table",
    primaryKeys = ["name", "instanceDomain"],
    foreignKeys = [ForeignKey(parentColumns = ["uri"], childColumns = ["instanceDomain"], entity = MetaDTO::class, onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)],
    indices = [Index("instanceDomain"), Index("name")]
)
data class EmojiDTO(
    val name: String,
    val instanceDomain: String,
    val host: String?,
    val url: String?,
    val uri: String?,
    val type: String?,
    val category: String?,
    val id: String?
){

    constructor(emoji: Emoji, instanceDomain: String) : this(
        name = emoji.name,
        instanceDomain = instanceDomain,
        host = emoji.host,
        url = emoji.url,
        uri = emoji.uri,
        type = emoji.type,
        category = emoji.category,
        id = emoji.id
    )

    fun toEmoji(): Emoji{
        return Emoji(
            name = this.name,
            id = this.id,
            uri = this.uri,
            url = this.url,
            type = this.type,
            category = this.category,
            host = this.host
        )
    }
}