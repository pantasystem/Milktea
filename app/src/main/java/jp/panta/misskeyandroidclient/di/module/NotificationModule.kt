package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.model.DataBase
import net.pantasystem.milktea.data.model.notification.NotificationDataSource
import net.pantasystem.milktea.data.model.notification.NotificationRepository
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.model.notification.impl.InMemoryNotificationDataSource
import net.pantasystem.milktea.data.model.notification.impl.MediatorNotificationDataSource
import net.pantasystem.milktea.data.model.notification.impl.NotificationRepositoryImpl
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