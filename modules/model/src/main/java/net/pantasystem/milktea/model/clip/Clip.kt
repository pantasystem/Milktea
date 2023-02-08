package net.pantasystem.milktea.model.clip

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User

data class Clip(
    val id: ClipId,
    val createdAt: Instant,
    val userId: User.Id,
    val name: String,
    val description: String?,
    val isPublic: Boolean,
)

data class ClipId(
    val accountId: Long,
    val clipId: String
)