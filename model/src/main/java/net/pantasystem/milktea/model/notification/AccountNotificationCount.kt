package net.pantasystem.milktea.model.notification

import androidx.room.Entity

@Entity
data class AccountNotificationCount(
    val accountId: Long,
    val count: Int
)
