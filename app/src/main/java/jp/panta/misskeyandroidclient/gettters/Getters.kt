package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.group.GroupDataSource
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class Getters(
    noteDataSource: NoteDataSource,
    noteRepository: NoteRepository,
    userDataSource: UserDataSource,
    notificationDataSource: NotificationDataSource,
    messageDataSource: MessageDataSource,
    groupDataSource: GroupDataSource,
    loggerFactory: Logger.Factory
) {
    val noteRelationGetter = NoteRelationGetter(noteDataSource, noteRepository, userDataSource, loggerFactory.create("NoteRelationGetter"))

    val notificationRelationGetter = NotificationRelationGetter(userDataSource, notificationDataSource, noteRelationGetter)

    val messageRelationGetter = MessageRelationGetter(messageDataSource, userDataSource, groupDataSource)
}