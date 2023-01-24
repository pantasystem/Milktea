package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.notification.impl.MediatorNotificationDataSource
import net.pantasystem.milktea.data.infrastructure.notification.impl.NotificationPagingStoreImpl
import net.pantasystem.milktea.data.infrastructure.notification.impl.NotificationRepositoryImpl
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.notification.NotificationPagingStore
import net.pantasystem.milktea.model.notification.NotificationRepository
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

    @Binds
    @Singleton
    abstract fun bindNotificationPagingStoreFactory(
        impl: NotificationPagingStoreImpl.Factory
    ) : NotificationPagingStore.Factory
}