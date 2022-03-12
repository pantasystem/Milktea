package jp.panta.misskeyandroidclient.model.notification

import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.users.User

data class NotificationRelation (
    val notification: Notification,
    val user: User?,
    val note: NoteRelation?
)