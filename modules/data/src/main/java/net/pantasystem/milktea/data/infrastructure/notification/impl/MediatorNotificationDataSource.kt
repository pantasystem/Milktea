package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotification
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationDataSource
import javax.inject.Inject

class MediatorNotificationDataSource @Inject constructor(
    private val unreadNotificationDAO: UnreadNotificationDAO
) : NotificationDataSource {
    @Inject lateinit var inMemoryNotificationDataSource: InMemoryNotificationDataSource
    override suspend fun add(notification: Notification): Result<AddResult> = runCancellableCatching {
        unreadNotificationDAO.delete(notification.id.accountId, notification.id.notificationId)
        if(!notification.isRead) {
            unreadNotificationDAO.insert(UnreadNotification(notification.id.accountId, notification.id.notificationId))
        }
        inMemoryNotificationDataSource.add(notification).getOrThrow()
    }

    override suspend fun addAll(notifications: Collection<Notification>): Result<List<AddResult>> = runCancellableCatching {
        notifications.map {
            add(it).getOrElse {
                AddResult.Canceled
            }
        }
    }

    override fun addEventListener(listener: NotificationDataSource.Listener) {
        inMemoryNotificationDataSource.addEventListener(listener)
    }


    override suspend fun get(notificationId: Notification.Id): Result<Notification> {
        return inMemoryNotificationDataSource.get(notificationId)
    }

    override suspend fun remove(notificationId: Notification.Id): Result<Boolean> {
        return inMemoryNotificationDataSource.remove(notificationId)
    }

    override fun removeEventListener(listener: NotificationDataSource.Listener) {
        inMemoryNotificationDataSource.removeEventListener(listener)
    }


}