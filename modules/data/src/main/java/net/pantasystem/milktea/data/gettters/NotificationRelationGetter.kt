package net.pantasystem.milktea.data.gettters

import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.data.infrastructure.toNotification
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.notes.NoteRelationGetter
import net.pantasystem.milktea.model.notification.*
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRelationGetter @Inject constructor(
    private val userDataSource: UserDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val noteRelationGetter: NoteRelationGetter,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val groupDataSource: GroupDataSource,
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
        notificationDTO.invitation?.group?.toGroup(account.accountId)?.let { group ->
            groupDataSource.add(group)
        }
        return NotificationRelation(
            notification,
            user,
            noteRelation?.getOrNull()
        )
    }

    suspend fun get(notificationId: Notification.Id): NotificationRelation {
        val notification = notificationDataSource.get(notificationId)
        val user = (notification.getOrThrow() as? HasUser)?.userId?.let {
            userDataSource.get(it)
        }
        val noteRelation = (notification.getOrThrow() as? HasNote)?.let{
            noteRelationGetter.get(it.noteId)
        }
        return NotificationRelation(notification.getOrThrow(), user?.getOrNull(), noteRelation?.getOrNull())
    }

    suspend fun get(accountId: Long, notificationId: String): NotificationRelation {
        return get(Notification.Id(accountId, notificationId))
    }

}