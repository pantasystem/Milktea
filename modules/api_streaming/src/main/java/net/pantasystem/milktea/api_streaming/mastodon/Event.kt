package net.pantasystem.milktea.api_streaming.mastodon

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.model.emoji.Emoji

sealed interface Event {

    data class Update(val status: TootStatusDTO) : Event
    data class Delete(val id: String) : Event
    data class Notification(val tmp: String) : Event
    data class Reaction(val reaction: EmojiReaction) : Event
}

// {
//  "name":"ablobattention",
//  "count":1,"url":"https://s3.fedibird.com/cache/custom_emojis/images/000/292/585/original/2613d831b8b93127.png",
//  "static_url":"https://s3.fedibird.com/cache/custom_emojis/images/000/292/585/static/2613d831b8b93127.png",
//  "domain":"misskey.life",
//  "account_ids":["109568651641736774"],
//  "status_id":"109724595650871529"
//  }
// {"name":"🎉","count":1,"account_ids":["109011018797559029"],"status_id":"109724742705742083"}
@kotlinx.serialization.Serializable
data class EmojiReaction(
    val name: String,
    val count: Int,
    val url: String? = null,
    @SerialName("static_url") val staticUrl: String? = null,
    @SerialName("domain") val domain: String? = null,
    @SerialName("account_ids") val accountIds: List<String>,
    @SerialName("status_id") val statusId: String
) {
    val isCustomEmoji: Boolean = url != null || staticUrl != null
    val reaction = if (isCustomEmoji) {
        if (domain == null) {
            ":$name@.:"
        } else {
            ":$name@$domain:"
        }
    } else {
        name
    }


    fun toEmoji(): Emoji? {
        if (!isCustomEmoji) {
            return null
        }

        return Emoji(
            name = if (domain == null) {
                ":$name@.:"
            } else {
                ":$name@$domain:"
            },
            url = url,
            host = domain,
        )
    }

    fun myReaction(myServerId: String): String? {
        return reaction.takeIf {
            accountIds.contains(myServerId)
        }
    }
}