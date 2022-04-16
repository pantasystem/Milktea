package net.pantasystem.milktea.data.model.notification

import androidx.room.Entity

@Entity
data class AccountNotificationCount(
    val accountId: Long,
    val count: Int
)
