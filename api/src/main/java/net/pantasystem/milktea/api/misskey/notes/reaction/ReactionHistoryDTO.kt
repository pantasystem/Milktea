package net.pantasystem.milktea.api.misskey.notes.reaction

import net.pantasystem.milktea.api.misskey.users.UserDTO
import java.util.*

data class ReactionHistoryDTO (
    val id: String,
    val createdAt: Date,
    val user: UserDTO,
    val type: String,
)