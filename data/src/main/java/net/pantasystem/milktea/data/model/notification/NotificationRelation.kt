package net.pantasystem.milktea.data.model.notification

import net.pantasystem.milktea.data.model.notes.NoteRelation
import net.pantasystem.milktea.data.model.users.User

data class NotificationRelation (
    val notification: Notification,
    val user: User?,
    val note: NoteRelation?
)