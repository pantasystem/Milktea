package net.pantasystem.milktea.model.notification

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    suspend fun read(notificationId: Notification.Id)

    fun countUnreadNotification(accountId: Long): Flow<Int>
}