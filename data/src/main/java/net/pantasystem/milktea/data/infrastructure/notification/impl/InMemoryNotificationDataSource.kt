package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.notification.NotificationNotFoundException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class InMemoryNotificationDataSource @Inject constructor() : NotificationDataSource {

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



    private suspend fun find(id: Notification.Id): Notification? {
        notificationsMapLock.withLock {
            return notificationIdAndNotification[id]
        }
    }

    private suspend fun createOrUpdate(notification: Notification): AddResult {
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



    private fun publish(event: NotificationDataSource.Event) = runBlocking{
        listenersLock.withLock {
            listeners.forEach {
                it.on(event)
            }
        }
    }
}