package net.pantasystem.milktea.model.notification

import net.pantasystem.milktea.model.notes.NoteRelationGetter
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NotificationRelationGetter @Inject constructor(
    private val userDataSource: UserDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val noteRelationGetter: NoteRelationGetter,
) {

    suspend fun get(notificationId: Notification.Id): NotificationRelation {
        val notification = notificationDataSource.get(notificationId)
        val user = (notification.getOrThrow() as? HasUser)?.userId?.let {
            userDataSource.get(it)
        }
        val noteRelation = (notification.getOrThrow() as? HasNote)?.let {
            noteRelationGetter.get(it.noteId)
        }
        return NotificationRelation(
            notification.getOrThrow(), user?.getOrNull(), noteRelation?.getOrNull()
        )
    }

    suspend fun get(accountId: Long, notificationId: String): NotificationRelation {
        return get(Notification.Id(accountId, notificationId))
    }

}