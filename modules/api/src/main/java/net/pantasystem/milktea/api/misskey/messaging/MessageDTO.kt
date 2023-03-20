package net.pantasystem.milktea.api.misskey.messaging

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.groups.GroupDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.model.emoji.Emoji
import java.io.Serializable as JavaSerializable

@Serializable
data class MessageDTO(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    @Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,

    @SerialName("text")
    val text: String? = null,

    @SerialName("userId")
    val userId: String,

    @SerialName("user")
    val user: UserDTO,

    @SerialName("recipientId")
    val recipientId: String? = null,

    @SerialName("recipient")
    val recipient: UserDTO? = null,

    @SerialName("groupId")
    val groupId: String? = null,

    @SerialName("group")
    val group: GroupDTO? = null,

    @SerialName("fileId")
    val fileId: String? = null,

    @SerialName("file")
    val file: FilePropertyDTO? = null,

    @SerialName("isRead")
    val isRead: Boolean,

    @SerialName("emojis")
    val emojis: List<Emoji>? = null,

    @SerialName("reads")
    val reads: List<String>? = null,
): JavaSerializable

