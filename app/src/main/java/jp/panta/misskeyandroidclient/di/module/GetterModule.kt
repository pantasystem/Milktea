package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.gettters.Getters
import net.pantasystem.milktea.data.gettters.MessageRelationGetter
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.data.gettters.NotificationRelationGetter
import net.pantasystem.milktea.data.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.data.model.group.GroupDataSource
import net.pantasystem.milktea.data.model.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.model.notes.NoteDataSource
import net.pantasystem.milktea.data.model.notes.NoteRepository
import net.pantasystem.milktea.data.model.notification.NotificationDataSource
import net.pantasystem.milktea.data.model.users.UserDataSource
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object GetterModule {

    @Singleton
    @Provides
    fun getters(
        noteDataSource: NoteDataSource,
        noteRepository: NoteRepository,
        userDataSource: UserDataSource,
        filePropertyDataSource: FilePropertyDataSource,
        notificationDataSource: NotificationDataSource,
        messageDataSource: MessageDataSource,
        groupDataSource: GroupDataSource,
        loggerFactory: net.pantasystem.milktea.common.Logger.Factory
    ): Getters {
        return Getters(
            noteDataSource,
            noteRepository,
            userDataSource,
            filePropertyDataSource,
            notificationDataSource,
            messageDataSource,
            groupDataSource,
            loggerFactory,
        )
    }

    @Singleton
    @Provides
    fun noteRelationGetter(getters: Getters): NoteRelationGetter {
        return getters.noteRelationGetter
    }

    @Singleton
    @Provides
    fun notificationRelationGetter(getters: Getters): NotificationRelationGetter {
        return getters.notificationRelationGetter
    }

    @Singleton
    @Provides
    fun messageRelationGetter(getters: Getters): MessageRelationGetter {
        return getters.messageRelationGetter
    }
}