package net.pantasystem.milktea.data.model.notification.impl

import net.pantasystem.milktea.data.model.AddResult
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.notification.Notification
import net.pantasystem.milktea.data.model.notification.NotificationDataSource
import net.pantasystem.milktea.data.model.notification.db.UnreadNotification
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import javax.inject.Inject

class MediatorNotificationDataSource @Inject constructor(
    private val unreadNotificationDAO: UnreadNotificationDAO
) : NotificationDataSource{
    @Inject lateinit var inMemoryNotificationDataSource: InMemoryNotificationDataSource
    override suspend fun add(notification: Notification): AddResult {
        unreadNotificationDAO.delete(notification.id.accountId, notification.id.notificationId)
        if(!notification.isRead) {
            unreadNotificationDAO.insert(UnreadNotification(notification.id.accountId, notification.id.notificationId))
        }
        return inMemoryNotificationDataSource.add(notification)
    }

    override suspend fun addAll(notifications: Collection<Notification>): List<AddResult> {
        return notifications.map {
            add(it)
        }
    }

    override fun addEventListener(listener: NotificationDataSource.Listener) {
        inMemoryNotificationDataSource.addEventListener(listener)
    }


    override suspend fun get(notificationId: Notification.Id): Notification {
        return inMemoryNotificationDataSource.get(notificationId)
    }

    override suspend fun remove(notificationId: Notification.Id): Boolean {
        return inMemoryNotificationDataSource.remove(notificationId)
    }

    override fun removeEventListener(listener: NotificationDataSource.Listener) {
        inMemoryNotificationDataSource.removeEventListener(listener)
    }


}