package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.users.UserRepository

class Getters(
    noteRepository: NoteRepository,
    userRepository: UserRepository,
    notificationRepository: NotificationRepository,
    messageDataSource: MessageDataSource
) {
    val noteRelationGetter = NoteRelationGetter(noteRepository, userRepository)

    val notificationRelationGetter = NotificationRelationGetter(userRepository, notificationRepository, noteRelationGetter)

    val messageRelationGetter = MessageRelationGetter(messageDataSource, userRepository)
}