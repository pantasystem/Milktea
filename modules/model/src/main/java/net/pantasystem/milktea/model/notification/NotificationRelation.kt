package net.pantasystem.milktea.model.notification

import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.user.User

data class NotificationRelation (
    val notification: Notification,
    val user: User?,
    val note: NoteRelation?
)