package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class Getters(
    noteDataSource: NoteDataSource,
    userDataSource: UserDataSource,
    notificationDataSource: NotificationDataSource,
    messageDataSource: MessageDataSource
) {
    val noteRelationGetter = NoteRelationGetter(noteDataSource, userDataSource)

    val notificationRelationGetter = NotificationRelationGetter(userDataSource, notificationDataSource, noteRelationGetter)

    val messageRelationGetter = MessageRelationGetter(messageDataSource, userDataSource)
}