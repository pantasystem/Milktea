package jp.panta.misskeyandroidclient.model.notification.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notification.AccountNotificationCount
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationNotFoundException
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryNotificationDataSource : NotificationDataSource{

    private val listeners = mutableSetOf<NotificationDataSource.Listener>()
    private val notificationIdAndNotification = mutableMapOf<Notification.Id, Notification>()

    private val notificationsMapLock = Mutex()
    private val listenersLock = Mutex()

    override fun addEventListener(listener: NotificationDataSource.Listener) = runBlocking <Unit>{
        listenersLock.withLock {
            listeners.add(listener)
        }
    }

    override fun removeEventListener(listener: NotificationDataSource.Listener) = runBlocking<Unit>{
        listenersLock.withLock {
            listeners.remove(listener)
        }
    }

    override suspend fun add(notification: Notification): AddResult {
        return createOrUpdate(notification).also {
            if(it == AddResult.CREATED) {
                publish(NotificationDataSource.Event.Created(notification.id, notification))
            }else if(it == AddResult.UPDATED) {
                publish(NotificationDataSource.Event.Updated(notification.id, notification))
            }
        }
    }

    override suspend fun addAll(notifications: Collection<Notification>): List<AddResult> {
        return notifications.map {
            add(it)
        }
    }

    override suspend fun get(notificationId: Notification.Id): Notification {
        return find(notificationId)?: throw NotificationNotFoundException(notificationId)
    }

    override suspend fun remove(notificationId: Notification.Id): Boolean {
        return delete(notificationId).also {
            if(it){
                publish(NotificationDataSource.Event.Deleted(notificationId))
            }
        }
    }






    private suspend fun countUnreadNotificationByAccount(accountId: Long): Int {
        notificationsMapLock.withLock {
            return notificationIdAndNotification.values.filter {
                it.id.accountId == accountId
            }.filterNot {
                it.isRead
            }.count()
        }
    }

    private suspend fun countUnreadNotification(): List<AccountNotificationCount> {
        notificationsMapLock.withLock {
            return notificationIdAndNotification.values.groupBy {
                it.id.accountId
            }.map {
                AccountNotificationCount(it.key, it.value.count())
            }
        }
    }

    private suspend fun find(id: Notification.Id): Notification? {
        notificationsMapLock.withLock {
            return notificationIdAndNotification[id]
        }
    }

    private suspend fun createOrUpdate(notification: Notification): AddResult{
        notificationsMapLock.withLock {
            val exNotification = notificationIdAndNotification[notification.id]
            notificationIdAndNotification[notification.id] = notification
            return if(exNotification == null) AddResult.CREATED else AddResult.UPDATED
        }
    }

    private suspend fun delete(notificationId: Notification.Id): Boolean {
        notificationsMapLock.withLock {
            val ex = notificationIdAndNotification[notificationId]
            notificationIdAndNotification.remove(notificationId)
            return ex != null
        }
    }

    private suspend fun findAllByAccountId(accountId: Long): List<Notification> {
        return notificationsMapLock.withLock {
            notificationIdAndNotification.values.filter {
                it.id.accountId == accountId
            }
        }
    }

    private fun publish(event: NotificationDataSource.Event) = runBlocking{
        listenersLock.withLock {
            listeners.forEach {
                it.on(event)
            }
        }
    }
}