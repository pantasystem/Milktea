package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.gettters.Getters
import jp.panta.misskeyandroidclient.gettters.NoteRelationGetter
import jp.panta.misskeyandroidclient.gettters.NotificationRelationGetter
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.group.GroupDataSource
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
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
        loggerFactory: Logger.Factory
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
}