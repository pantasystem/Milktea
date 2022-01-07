package jp.panta.misskeyandroidclient.di.module.user

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.model.users.impl.InMemoryUserDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Singleton
    @Provides
    fun userDataSource(loggerFactory: Logger.Factory): UserDataSource {
        return InMemoryUserDataSource(loggerFactory)
    }
}