package net.pantasystem.milktea.data.gettters

import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.data.model.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.model.toNotification
import net.pantasystem.milktea.data.model.toUser
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notification.*

class NotificationRelationGetter(
    private val userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val noteRelationGetter: NoteRelationGetter,
    private val noteDataSourceAdder: NoteDataSourceAdder
) {


    suspend fun get(account: Account, notificationDTO: NotificationDTO): NotificationRelation {
        val user = notificationDTO.user?.toUser(account, false)
        if (user != null) {
            userDataSource.add(user)
        }
        val noteRelation = notificationDTO.note?.let{
            noteRelationGetter.get(noteDataSourceAdder.addNoteDtoToDataSource(account, it))
        }
        val notification = notificationDTO.toNotification(account)
        notificationDataSource.add(notification)
        return NotificationRelation(
            notification,
            user,
            noteRelation
        )
    }

    suspend fun get(notificationId: Notification.Id): NotificationRelation {
        val notification = notificationDataSource.get(notificationId)
        val user = (notification as? HasUser)?.userId?.let {
            userDataSource.get(it)
        }
        val noteRelation = (notification as? HasNote)?.let{
            noteRelationGetter.get(it.noteId)
        }
        return NotificationRelation(notification, user, noteRelation)
    }

    suspend fun get(accountId: Long, notificationId: String): NotificationRelation {
        return get(Notification.Id(accountId, notificationId))
    }

}