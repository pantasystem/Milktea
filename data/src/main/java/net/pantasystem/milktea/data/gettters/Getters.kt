package net.pantasystem.milktea.data.gettters

import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Getters @Inject constructor(
    noteDataSource: NoteDataSource,
    userDataSource: UserDataSource,
    filePropertyDataSource: FilePropertyDataSource,
    notificationDataSource: NotificationDataSource,
    val noteRelationGetter: NoteRelationGetter,
) {


    val notificationRelationGetter = NotificationRelationGetter(
        userDataSource,
        notificationDataSource,
        noteRelationGetter,
        noteDataSourceAdder = NoteDataSourceAdder(
            userDataSource, noteDataSource, filePropertyDataSource
        )
    )

}