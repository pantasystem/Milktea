package net.pantasystem.milktea.data.gettters

import net.pantasystem.milktea.common.Logger
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.group.GroupDataSource
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteDataSourceAdder
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Getters @Inject constructor(
    noteDataSource: NoteDataSource,
    noteRepository: NoteRepository,
    userDataSource: UserDataSource,
    filePropertyDataSource: FilePropertyDataSource,
    notificationDataSource: NotificationDataSource,
    messageDataSource: MessageDataSource,
    groupDataSource: GroupDataSource,
    loggerFactory: net.pantasystem.milktea.common.Logger.Factory
) {
    val noteRelationGetter = NoteRelationGetter(noteRepository, userDataSource, filePropertyDataSource, loggerFactory.create("NoteRelationGetter"))

    val notificationRelationGetter = NotificationRelationGetter(userDataSource, notificationDataSource, noteRelationGetter, noteDataSourceAdder = NoteDataSourceAdder(
        userDataSource, noteDataSource, filePropertyDataSource
    ))

    val messageRelationGetter = MessageRelationGetter(messageDataSource, userDataSource, groupDataSource)
}