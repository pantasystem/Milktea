package net.pantasystem.milktea.model.user.mute

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User

data class CreateMute(
    val userId: User.Id,
    val expiresAt: Instant? = null,
)
