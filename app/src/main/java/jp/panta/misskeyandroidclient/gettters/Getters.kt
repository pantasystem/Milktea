package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.users.UserRepository

class Getters(
    noteRepository: NoteRepository,
    userRepository: UserRepository,
    notificationRepository: NotificationRepository
) {
    val noteRelationGetter = NoteRelationGetter(noteRepository, userRepository)

    val notificationRelationGetter = NotificationRelationGetter(userRepository, notificationRepository, noteRelationGetter)
}