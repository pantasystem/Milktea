package jp.panta.misskeyandroidclient.viewmodel.notification

import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class NotificationViewData(private val notification: Notification) {
    val id = notification.id
    val noteViewData: PlaneNoteViewData? = if(notification.note == null) null else PlaneNoteViewData(notification.note)

    val statusType: String = notification.type

    //val user: User = notification.user
    val avatarIconUrl = notification.user.avatarUrl
    val name = notification.user.name
    val userName = notification.user.userName

    val reaction =  notification.reaction
}