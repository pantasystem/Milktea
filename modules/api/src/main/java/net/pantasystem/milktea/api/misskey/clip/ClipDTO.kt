package net.pantasystem.milktea.api.misskey.clip

import kotlinx.datetime.Instant
import net.pantasystem.milktea.api.misskey.users.UserDTO

@kotlinx.serialization.Serializable
data class ClipDTO(
    val id: String,
    val createdAt: Instant,
    val userId: String,
    val user: UserDTO,
    val name: String,
    val description: String? = null,
    val isPublic: Boolean
)