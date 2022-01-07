package jp.panta.misskeyandroidclient.di.module.notifications

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.model.notification.impl.InMemoryNotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.impl.MediatorNotificationDataSource
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class NotificationModule {

    @Binds
    abstract fun notificationDataSource(
        ds: MediatorNotificationDataSource,
    ): NotificationDataSource


}