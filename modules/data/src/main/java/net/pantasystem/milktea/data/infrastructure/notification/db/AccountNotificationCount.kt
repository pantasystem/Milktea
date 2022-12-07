package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.Entity

@Entity
data class AccountNotificationCount(
    val accountId: Long,
    val count: Int
)
