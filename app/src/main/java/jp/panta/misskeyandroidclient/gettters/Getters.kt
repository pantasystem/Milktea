package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class Getters(
    noteRepository: NoteRepository,
    userDataSource: UserDataSource,
    notificationRepository: NotificationRepository,
    messageDataSource: MessageDataSource
) {
    val noteRelationGetter = NoteRelationGetter(noteRepository, userDataSource)

    val notificationRelationGetter = NotificationRelationGetter(userDataSource, notificationRepository, noteRelationGetter)

    val messageRelationGetter = MessageRelationGetter(messageDataSource, userDataSource)
}