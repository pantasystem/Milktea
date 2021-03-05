package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.api.notification.NotificationDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notification.HasNote
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationRelation
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserRepository

class NotificationRelationGetter(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val noteRelationGetter: NoteRelationGetter
) {


    suspend fun get(account: Account, notificationDTO: NotificationDTO): NotificationRelation {
        val user = notificationDTO.user.toUser(account)
        userRepository.add(user)
        val noteRelation = notificationDTO.note?.let{
            noteRelationGetter.get(account, it)
        }
        val notification = notificationDTO.toNotification(account)
        notificationRepository.add(notification)
        return NotificationRelation(
            notification,
            user,
            noteRelation
        )
    }

    suspend fun get(notificationId: Notification.Id): NotificationRelation {
        val notification = notificationRepository.get(notificationId)
        val user = userRepository.get(notification.userId)
        val noteRelation = (notification as? HasNote)?.let{
            noteRelationGetter.get(it.noteId)
        }
        return NotificationRelation(notification, user, noteRelation)
    }

    suspend fun get(accountId: Long, notificationId: String): NotificationRelation {
        return get(Notification.Id(accountId, notificationId))
    }

}