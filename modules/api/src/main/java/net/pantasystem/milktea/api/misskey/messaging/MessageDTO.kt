package net.pantasystem.milktea.api.misskey.messaging

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.groups.GroupDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.model.emoji.Emoji
import java.io.Serializable as JavaSerializable

@Serializable
data class MessageDTO(
    val id: String,
    @Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    val text: String? = null,
    val userId: String,
    val user: UserDTO,
    val recipientId: String? = null,
    val recipient: UserDTO? = null,
    val groupId: String? = null,
    val group: GroupDTO? = null,
    val fileId: String? = null,
    val file: FilePropertyDTO? = null,
    val isRead: Boolean,
    val emojis: List<Emoji>? = null,
    val reads: List<String>? = null,
): JavaSerializable

