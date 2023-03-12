package net.pantasystem.milktea.model.user.renote.mute

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User

data class RenoteMute(
    val id: RenoteMuteId,
    val userId: User.Id,
    val createdAt: Instant,
    val postedAt: Instant?
)

data class RenoteMuteId(
    val accountId: Long,
    val id: String
)

