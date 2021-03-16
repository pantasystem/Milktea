package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class Getters(
    noteDataSource: NoteDataSource,
    userDataSource: UserDataSource,
    notificationRepository: NotificationRepository,
    messageDataSource: MessageDataSource
) {
    val noteRelationGetter = NoteRelationGetter(noteDataSource, userDataSource)

    val notificationRelationGetter = NotificationRelationGetter(userDataSource, notificationRepository, noteRelationGetter)

    val messageRelationGetter = MessageRelationGetter(messageDataSource, userDataSource)
}