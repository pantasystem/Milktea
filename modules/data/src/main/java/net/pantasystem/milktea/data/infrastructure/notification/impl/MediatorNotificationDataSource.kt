package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationCacheDAO
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationEntity
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationWithDetails
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotification
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationDataSource
import javax.inject.Inject

class MediatorNotificationDataSource @Inject constructor(
    private val unreadNotificationDAO: UnreadNotificationDAO,
    private val notificationCacheDAO: NotificationCacheDAO,
) : NotificationDataSource {
    override suspend fun add(notification: Notification): Result<AddResult> =
        runCancellableCatching {
            unreadNotificationDAO.delete(notification.id.accountId, notification.id.notificationId)
            if (!notification.isRead) {
                unreadNotificationDAO.insert(
                    UnreadNotification(
                        notification.id.accountId,
                        notification.id.notificationId
                    )
                )
            }
            insertAll(
                listOf(notification)
            )
            AddResult.Created
        }

    override suspend fun addAll(notifications: Collection<Notification>): Result<List<AddResult>> =
        runCancellableCatching {
            insertAll(notifications.toList())
            notifications.map {
                AddResult.Created
            }
        }

    override fun addEventListener(listener: NotificationDataSource.Listener) {
    }


    override suspend fun get(notificationId: Notification.Id): Result<Notification> =
        runCancellableCatching {
            val entity = notificationCacheDAO.findNotification(
                NotificationEntity.makeId(
                    notificationId.accountId,
                    notificationId.notificationId
                )
            )
            entity?.toModel() ?: throw NoSuchElementException("Notification not found")
        }

    override suspend fun remove(notificationId: Notification.Id): Result<Boolean> =
        runCancellableCatching {
            val count =
                notificationCacheDAO.exists(
                    NotificationEntity.makeId(
                        notificationId.accountId,
                        notificationId.notificationId
                    )
                )
            notificationCacheDAO.remove(
                NotificationEntity.makeId(
                    notificationId.accountId,
                    notificationId.notificationId
                )
            )
            count > 0
        }

    override fun removeEventListener(listener: NotificationDataSource.Listener) {
    }

    override fun observeIn(notificationIds: List<Notification.Id>): Flow<List<Notification>> {
        return notificationCacheDAO.observeIn(notificationIds.map {
            NotificationEntity.makeId(
                it.accountId,
                it.notificationId
            )
        }).map { list ->
            list.map { it.toModel() }
        }
    }

    override fun observeOne(notificationId: Notification.Id): Flow<Notification?> {
        return notificationCacheDAO.observeOne(
            notificationId.accountId,
            notificationId.notificationId
        )
            .map { it?.toModel() }
    }

    private suspend fun insertAll(notifications: List<Notification>) {
        val relations = notifications.map {
            NotificationWithDetails.fromModel(it)
        }
        notificationCacheDAO.insertAll(relations.map { it.notification })
        notificationCacheDAO.insertFollowNotifications(relations.mapNotNull { it.followNotification })
        notificationCacheDAO.insertNoteNotifications(relations.mapNotNull { it.noteNotification })
        notificationCacheDAO.insertReactionNotifications(relations.mapNotNull { it.reactionNotification })
        notificationCacheDAO.insertPollVoteNotifications(relations.mapNotNull { it.pollVoteNotification })
        notificationCacheDAO.insertGroupInvitedNotifications(relations.mapNotNull { it.groupInvitedNotification })
        notificationCacheDAO.insertUnknownNotifications(relations.mapNotNull { it.unknownNotification })

        val reads = notifications.filter { it.isRead }.map {
            UnreadNotification(it.id.accountId, it.id.notificationId)
        }
        val unReadNotificationIds = notifications.filter { !it.isRead }.map {
            it.id
        }.groupBy { it.accountId }

        unreadNotificationDAO.insertAll(reads)

        unReadNotificationIds.forEach { (accountId, ids) ->
            unreadNotificationDAO.deleteIn(accountId, ids.map { it.notificationId })
        }

    }

}