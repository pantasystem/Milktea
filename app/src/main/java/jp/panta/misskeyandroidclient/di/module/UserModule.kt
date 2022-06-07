package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.user.impl.FollowFollowerPagingStoreImpl
import net.pantasystem.milktea.data.infrastructure.user.impl.InMemoryUserDataSource
import net.pantasystem.milktea.data.infrastructure.user.impl.UserRepositoryImpl
import net.pantasystem.milktea.model.user.FollowFollowerPagingStore
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    @Singleton
    abstract fun userDataSource(dataSource: InMemoryUserDataSource): UserDataSource

    @Binds
    @Singleton
    abstract fun userRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun provideFollowFollowerPagingStoreFactory(
        impl: FollowFollowerPagingStoreImpl.Factory
    ) : FollowFollowerPagingStore.Factory
}