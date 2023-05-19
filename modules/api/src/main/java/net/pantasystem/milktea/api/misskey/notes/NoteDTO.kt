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
    @SerialName("id")
    val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class)
    @SerialName("createdAt")
    val createdAt: Instant,

    @SerialName("text")
    val text: String? = null,

    @SerialName("cw")
    val cw: String? = null,

    @SerialName("userId")
    val userId: String,

    @SerialName("replyId")
    val replyId: String? = null,

    @SerialName("renoteId")
    val renoteId: String? = null,

    @SerialName("viaMobile")
    val viaMobile: Boolean? = null,

    @SerialName("visibility")
    val visibility: NoteVisibilityType? = null,

    @SerialName("localOnly")
    val localOnly: Boolean? = null,

    @SerialName("visibleUserIds")
    val visibleUserIds: List<String>? = null,

    @kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class)
    @SerialName("reactionEmojis")
    val rawReactionEmojis: EmojisType? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("uri")
    val uri: String? = null,

    @SerialName("renoteCount")
    val renoteCount: Int,

    @SerialName("reactions")
    val reactionCounts: LinkedHashMap<String, Int>? = null,

    @kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class)
    @SerialName("emojis")
    val rawEmojis: EmojisType? = null,

    @SerialName("repliesCount")
    val replyCount: Int,

    @SerialName("user")
    val user: UserDTO,

    @SerialName("files")
    val files: List<FilePropertyDTO>? = null,

    @SerialName("fileIds")
    val fileIds: List<String>? = null,

    @SerialName("poll")
    val poll: PollDTO? = null,

    @SerialName("renote")
    val reNote: NoteDTO? = null,

    @SerialName("reply")
    val reply: NoteDTO? = null,

    @SerialName("myReaction")
    val myReaction: String? = null,


    @SerialName("_featuredId_")
    val tmpFeaturedId: String? = null,

    @SerialName("_prId_")
    val promotionId: String? = null,

    @SerialName("channelId")
    val channelId: String? = null,

    @SerialName("app")
    val app: App? = null,

    @SerialName("channel")
    val channel: ChannelInfo? = null,

    @SerialName("reactionAcceptance")
    val reactionAcceptance: ReactionAcceptanceType? = null,
) : Serializable {

    @kotlinx.serialization.Serializable
    data class ChannelInfo(
        @SerialName("id")
        val id: String,

        @SerialName("name")
        val name: String,
    ) : Serializable

    val reactionEmojiList = when(val emojis = rawReactionEmojis) {
        EmojisType.None -> emptyList()
        is EmojisType.TypeArray -> emojis.emojis
        is EmojisType.TypeObject -> emojis.emojis.map {
            Emoji(name = it.key, url = it.value)
        }
        null -> emptyList()
    }
    val emojiList: List<Emoji> = when(rawEmojis) {
        EmojisType.None -> emptyList()
        is EmojisType.TypeArray -> rawEmojis.emojis
        is EmojisType.TypeObject -> (rawEmojis.emojis).map {
            Emoji(name = it.key, url = it.value, uri = it.value)
        }
        null -> emptyList()
    } + reactionEmojiList
}


@kotlinx.serialization.Serializable(with = NoteVisibilityTypeSerializer::class)
enum class NoteVisibilityType {
    @SerialName("public") Public,
    @SerialName("home") Home,
    @SerialName("followers") Followers,
    @SerialName("specified") Specified
}

object NoteVisibilityTypeSerializer : EnumIgnoreUnknownSerializer<NoteVisibilityType>(NoteVisibilityType.values(), NoteVisibilityType.Public)

@kotlinx.serialization.Serializable
enum class ReactionAcceptanceType {
    @SerialName("likeOnly") LikeOnly,
    @SerialName("likeOnlyForRemote") LikeOnly4Remote,
    @SerialName("nonSensitiveOnly") NonSensitiveOnly,
    @SerialName("nonSensitiveOnlyForLocalLikeOnlyForRemote") NonSensitiveOnly4LocalOnly4Remote
}