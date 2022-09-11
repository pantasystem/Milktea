package net.pantasystem.milktea.api.misskey.notification

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.groups.InvitationDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO

@Serializable
data class NotificationDTO(
    val id: String,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    val type: String,
    val userId: String? = null,
    val user: UserDTO? = null,
    val note: NoteDTO? = null,
    val noteId: String? = null,
    val reaction: String? = null,
    val isRead: Boolean? = null,
    val choice: Int? = null,
    val invitation: InvitationDTO? = null,
)