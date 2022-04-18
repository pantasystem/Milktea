package net.pantasystem.milktea.api.misskey.notes

import com.google.gson.annotations.SerializedName
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.data.model.auth.custom.App
import net.pantasystem.milktea.data.model.emoji.Emoji
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
    val visibility: String? = null,
    val localOnly: Boolean? = null,

    @SerializedName("visibleUserIds")
    val visibleUserIds: List<String>? = null,

    val url: String? = null,
    val uri: String? = null,

    val renoteCount: Int,

    @SerializedName("reactions")
    @SerialName("reactions")
    val reactionCounts: LinkedHashMap<String, Int>? = null,

    @SerializedName("emojis") val emojis: List<Emoji>? = null,

    @SerializedName("repliesCount")
    @SerialName("repliesCount")
    val replyCount: Int,
    val user: UserDTO,
    val files: List<FilePropertyDTO>? = null,
    //@JsonProperty("fileIds") val mediaIds: List<String?>? = null,    //v10, v11の互換性が取れない
    val fileIds: List<String>? = null,
    val poll: PollDTO? = null,
    @SerializedName("renote")
    @SerialName("renote")
    val reNote: NoteDTO? = null,
    val reply: NoteDTO? = null,

    @SerializedName("myReaction")
    val myReaction: String? = null,

    @SerializedName("_featuredId_")
    @SerialName("_featuredId_")
    val tmpFeaturedId: String? = null,
    @SerialName("_prId_")
    @SerializedName("_prId_")
    val promotionId: String? = null,
    val channelId: String? = null,

    val app: App? = null
): Serializable



