package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.model.notification.impl.InMemoryNotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.impl.MediatorNotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.impl.NotificationRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun notificationDataSource(
        ds: MediatorNotificationDataSource,
    ): NotificationDataSource


    @Binds
    @Singleton
    abstract fun notificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository
}