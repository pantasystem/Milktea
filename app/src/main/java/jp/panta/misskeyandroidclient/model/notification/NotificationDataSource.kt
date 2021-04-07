package jp.panta.misskeyandroidclient.model.notification

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.Account

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

    suspend fun get(notificationId: Notification.Id): Notification
    suspend fun add(notification: Notification): AddResult
    suspend fun remove(notificationId: Notification.Id) : Boolean
    suspend fun addAll(notifications: Collection<Notification>): List<AddResult>

    suspend fun countUnreadNotification(accountId: Long): Int
    suspend fun readAllNotification(accountId: Long)

}