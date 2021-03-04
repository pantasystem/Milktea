package jp.panta.misskeyandroidclient.model.notification.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationNotFoundException
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository

class InMemoryNotificationRepository : NotificationRepository{

    private val listeners = mutableSetOf<NotificationRepository.Listener>()
    private val notificationIdAndNotification = mutableMapOf<Notification.Id, Notification>()

    override fun addEventListener(listener: NotificationRepository.Listener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override fun removeEventListener(listener: NotificationRepository.Listener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    override suspend fun add(notification: Notification): AddResult {
        return createOrUpdate(notification).also {
            if(it == AddResult.CREATED) {
                publish(NotificationRepository.Event.Created(notification.id, notification))
            }else if(it == AddResult.UPDATED) {
                publish(NotificationRepository.Event.Updated(notification.id, notification))
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
                publish(NotificationRepository.Event.Deleted(notificationId))
            }
        }
    }

    private fun find(id: Notification.Id): Notification? {
        synchronized(notificationIdAndNotification) {
            return notificationIdAndNotification[id]
        }
    }

    private fun createOrUpdate(notification: Notification): AddResult{
        synchronized(notificationIdAndNotification) {
            val exNotification = notificationIdAndNotification[notification.id]
            notificationIdAndNotification[notification.id] = notification
            return if(exNotification == null) AddResult.CREATED else AddResult.UPDATED
        }
    }

    private fun delete(notificationId: Notification.Id): Boolean {
        synchronized(notificationIdAndNotification) {
            val ex = notificationIdAndNotification[notificationId]
            notificationIdAndNotification.remove(notificationId)
            return ex != null
        }
    }

    private fun publish(event: NotificationRepository.Event) {
        synchronized(listeners) {
            listeners.forEach {
                it.on(event)
            }
        }
    }
}