package jp.panta.misskeyandroidclient.model.notification.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotification
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO

class MediatorNotificationDataSource(
    private val inMemoryNotificationDataSource: InMemoryNotificationDataSource,
    private val unreadNotificationDAO: UnreadNotificationDAO
) : NotificationDataSource{

    override suspend fun add(notification: Notification): AddResult {
        if(notification.isRead) {
            unreadNotificationDAO.delete(notification.id.accountId, notification.id.notificationId)
        } else {
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

    override suspend fun countUnreadNotification(accountId: Long): Int {
        return unreadNotificationDAO.countByAccountId(accountId)
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