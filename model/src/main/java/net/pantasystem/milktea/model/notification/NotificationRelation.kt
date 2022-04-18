package net.pantasystem.milktea.model.notification

import net.pantasystem.milktea.model.notes.NoteRelation

data class NotificationRelation (
    val notification: Notification,
    val user: net.pantasystem.milktea.model.user.User?,
    val note: NoteRelation?
)