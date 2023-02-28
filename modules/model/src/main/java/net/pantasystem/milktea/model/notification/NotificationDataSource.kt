package net.pantasystem.milktea.model.notification

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.AddResult


interface NotificationDataSource {

    sealed class Event {
        abstract val notificationId: Notification.Id
        data class Created(override val notificationId: Notification.Id, val notification: Notification) : Event()
        data class Updated(override val notificationId: Notification.Id, val notification: Notification) : Event()
        data class Deleted(override val notificationId: Notification.Id) : Event()
    }

    fun interface Listener {
        fun on(event: Event)
    }

    fun addEventListener(listener: Listener)
    fun removeEventListener(listener: Listener)

    suspend fun get(notificationId: Notification.Id): Result<Notification>
    suspend fun add(notification: Notification): Result<AddResult>
    suspend fun remove(notificationId: Notification.Id) : Result<Boolean>
    suspend fun addAll(notifications: Collection<Notification>): Result<List<AddResult>>

    fun observeOne(notificationId: Notification.Id): Flow<Notification?>
    fun observeIn(notificationIds: List<Notification.Id>): Flow<List<Notification>>


}