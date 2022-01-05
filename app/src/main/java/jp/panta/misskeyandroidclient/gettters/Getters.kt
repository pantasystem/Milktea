package jp.panta.misskeyandroidclient.gettters

import com.google.android.exoplayer2.upstream.FileDataSource
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.group.GroupDataSource
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteDataSourceAdder
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class Getters(
    noteDataSource: NoteDataSource,
    noteRepository: NoteRepository,
    userDataSource: UserDataSource,
    filePropertyDataSource: FilePropertyDataSource,
    notificationDataSource: NotificationDataSource,
    messageDataSource: MessageDataSource,
    groupDataSource: GroupDataSource,
    loggerFactory: Logger.Factory
) {
    val noteRelationGetter = NoteRelationGetter(noteRepository, userDataSource, filePropertyDataSource, loggerFactory.create("NoteRelationGetter"))

    val notificationRelationGetter = NotificationRelationGetter(userDataSource, notificationDataSource, noteRelationGetter, noteDataSourceAdder = NoteDataSourceAdder(
        userDataSource, noteDataSource, filePropertyDataSource
    ))

    val messageRelationGetter = MessageRelationGetter(messageDataSource, userDataSource, groupDataSource)
}