package net.pantasystem.milktea.model.user.renote.mute

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User

data class RenoteMute(
    val userId: User.Id,
    val createdAt: Instant,
    val postedAt: Instant?,
)
