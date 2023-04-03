package net.pantasystem.milktea.api.misskey.notification

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.groups.InvitationDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO

@Serializable
data class NotificationDTO(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,

    @SerialName("type")
    val type: String,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("user")
    val user: UserDTO? = null,

    @SerialName("note")
    val note: NoteDTO? = null,

    @SerialName("noteId")
    val noteId: String? = null,

    @SerialName("reaction")
    val reaction: String? = null,

    @SerialName("isRead")
    val isRead: Boolean? = null,

    @SerialName("choice")
    val choice: Int? = null,

    @SerialName("invitation")
    val invitation: InvitationDTO? = null,
)