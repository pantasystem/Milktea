package net.pantasystem.milktea.api.misskey.notes


import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.auth.App
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.emoji.CustomEmojisTypeSerializer
import net.pantasystem.milktea.api.misskey.emoji.EmojisType
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.common.serializations.EnumIgnoreUnknownSerializer
import net.pantasystem.milktea.model.emoji.Emoji
import java.io.Serializable

@kotlinx.serialization.Serializable
data class NoteDTO(
    val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    val text: String? = null,
    val cw: String? = null,
    val userId: String,

    val replyId: String? = null,

    val renoteId: String? = null,

    val viaMobile: Boolean? = null,
    val visibility: NoteVisibilityType? = null,
    val localOnly: Boolean? = null,

    @SerialName("visibleUserIds")
    val visibleUserIds: List<String>? = null,

    val reactionEmojis: Map<String, String>? = null,

    val url: String? = null,
    val uri: String? = null,

    val renoteCount: Int,

    @SerialName("reactions")
    val reactionCounts: LinkedHashMap<String, Int>? = null,

    @kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class)
    @SerialName("emojis") val rawEmojis: EmojisType? = null,

    @SerialName("repliesCount")
    val replyCount: Int,
    val user: UserDTO,
    val files: List<FilePropertyDTO>? = null,
    //@JsonProperty("fileIds") val mediaIds: List<String?>? = null,    //v10, v11の互換性が取れない
    val fileIds: List<String>? = null,
    val poll: PollDTO? = null,
    @SerialName("renote")
    val reNote: NoteDTO? = null,
    val reply: NoteDTO? = null,

    @SerialName("myReaction")
    val myReaction: String? = null,


    @SerialName("_featuredId_")
    val tmpFeaturedId: String? = null,
    @SerialName("_prId_")

    val promotionId: String? = null,
    val channelId: String? = null,

    val app: App? = null,
    val channel: ChannelInfo? = null,
) : Serializable {

    @kotlinx.serialization.Serializable
    data class ChannelInfo(
        val id: String,
        val name: String,
    ) : Serializable

    val emojiList: List<Emoji>? = when(rawEmojis) {
        EmojisType.None -> null
        is EmojisType.TypeArray -> rawEmojis.emojis
        is EmojisType.TypeObject -> (rawEmojis.emojis + (reactionEmojis ?: emptyMap())).map {
            Emoji(name = it.key, url = it.value, uri = it.value)
        }
        null -> null
    }
}


@kotlinx.serialization.Serializable(with = NoteVisibilityTypeSerializer::class)
enum class NoteVisibilityType {
    @SerialName("public") Public, @SerialName("home") Home, @SerialName("followers") Followers, @SerialName("specified") Specified
}

object NoteVisibilityTypeSerializer : EnumIgnoreUnknownSerializer<NoteVisibilityType>(NoteVisibilityType.values(), NoteVisibilityType.Public)
