package net.pantasystem.milktea.api.misskey.v12.user.reaction

import kotlinx.datetime.Instant
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO

@kotlinx.serialization.Serializable
data class UserReaction(
    val id: String,
    val type: String,
    val user: UserDTO,
    val note: NoteDTO,
    val createdAt: Instant,
)
