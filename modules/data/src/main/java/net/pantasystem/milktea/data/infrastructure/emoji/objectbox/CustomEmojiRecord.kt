package net.pantasystem.milktea.data.infrastructure.emoji.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import net.pantasystem.milktea.model.emoji.Emoji

@Entity
data class CustomEmojiRecord(
    @Id var id: Long = 0L,
    var serverId: String? = null,

    @Index var name: String = "",

    @Index var emojiHost: String = "",

    var url: String? = null,

    var uri: String? = null,

    var type: String? = null,

    var category: String? = null,

    var aliases: MutableList<String> = mutableListOf()
) {
    companion object {
        fun from(model: Emoji, host: String): CustomEmojiRecord {
            val record = CustomEmojiRecord()
            record.applyModel(model, host)
            return record
        }
    }

    fun applyModel(model: Emoji, host: String) {
        serverId = model.id
        name = model.name
        emojiHost = host
        url = model.url
        uri = model.uri
        type = model.type
        category = model.category
        aliases = model.aliases?.toMutableList() ?: mutableListOf()
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
            aliases = aliases,
            aspectRatio = aspectRatio,
            cachePath = cachePath,
        )
    }
}