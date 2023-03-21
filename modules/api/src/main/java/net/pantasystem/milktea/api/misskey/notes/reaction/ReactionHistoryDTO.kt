package net.pantasystem.milktea.api.misskey.notes.reaction

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.users.UserDTO

@Serializable
data class ReactionHistoryDTO(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    val createdAt: Instant,

    @SerialName("user")
    val user: UserDTO,

    @SerialName("type")
    val type: String,
)