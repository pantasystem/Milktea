package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.notification.NotificationNotFoundException
import javax.inject.Inject

class InMemoryNotificationDataSource @Inject constructor() : NotificationDataSource {

    private val _state = MutableStateFlow<NotificationsWrapper>(NotificationsWrapper(emptyMap()))
    private val listeners = mutableSetOf<NotificationDataSource.Listener>()
    private val notificationIdAndNotification = mutableMapOf<Notification.Id, Notification>()

    private val notificationsMapLock = Mutex()
    private val listenersLock = Mutex()

    init {
        addEventListener {
            _state.value = NotificationsWrapper(notificationIdAndNotification)
        }
    }

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

    override suspend fun add(notification: Notification): Result<AddResult> = runCancellableCatching {
        createOrUpdate(notification).also {
            if(it == AddResult.Created) {
                publish(NotificationDataSource.Event.Created(notification.id, notification))
            }else if(it == AddResult.Updated) {
                publish(NotificationDataSource.Event.Updated(notification.id, notification))
            }
        }
    }

    override suspend fun addAll(notifications: Collection<Notification>): Result<List<AddResult>> = runCancellableCatching {
        notifications.map {
            add(it).getOrElse {
                AddResult.Canceled
            }
        }
    }

    override suspend fun get(notificationId: Notification.Id): Result<Notification> = runCancellableCatching {
        find(notificationId)?: throw NotificationNotFoundException(notificationId)
    }

    override suspend fun remove(notificationId: Notification.Id): Result<Boolean> = runCancellableCatching {
        delete(notificationId).also {
            if(it){
                publish(NotificationDataSource.Event.Deleted(notificationId))
            }
        }
    }

    override fun observeIn(notificationIds: List<Notification.Id>): Flow<List<Notification>> {
        return _state.map { map ->
            notificationIds.mapNotNull {
                map.notifications[it]
            }
        }.distinctUntilChanged()
    }

    override fun observeOne(notificationId: Notification.Id): Flow<Notification?> {
        return _state.map {
            it.notifications[notificationId]
        }.distinctUntilChanged()
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
            return if(exNotification == null) AddResult.Created else AddResult.Updated
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

private data class NotificationsWrapper(
    val notifications: Map<Notification.Id, Notification>
)