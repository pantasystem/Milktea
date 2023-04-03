package net.pantasystem.milktea.api.misskey.v12.user.reaction

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO

@kotlinx.serialization.Serializable
data class UserReaction(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String,

    @SerialName("user")
    val user: UserDTO,

    @SerialName("note")
    val note: NoteDTO,

    @SerialName("createdAt")
    val createdAt: Instant,
)
