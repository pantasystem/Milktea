package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.app_store.user.FollowFollowerPagingStore
import net.pantasystem.milktea.app_store.user.UserReactionPagingStore
import net.pantasystem.milktea.data.infrastructure.user.*
import net.pantasystem.milktea.model.user.FollowRequestRepository
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    @Singleton
    abstract fun userDataSource(dataSource: MediatorUserDataSource): UserDataSource

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

    @Binds
    @Singleton
    abstract fun provideUserReactionPagingStoreFactory(
        impl: UserReactionPagingStoreImpl.Factory
    ) : UserReactionPagingStore.Factory

    @Binds
    @Singleton
    abstract fun bindFollowRequestRepository(
        impl: FollowRequestRepositoryImpl
    ): FollowRequestRepository
}