package net.pantasystem.milktea.api.misskey.notes.reaction

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.users.UserDTO

@Serializable
data class ReactionHistoryDTO (
    val id: String,
    val createdAt: Instant,
    val user: UserDTO,
    val type: String,
)