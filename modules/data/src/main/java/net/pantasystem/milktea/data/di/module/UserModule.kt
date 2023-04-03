package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.app_store.user.FollowFollowerPagingStore
import net.pantasystem.milktea.app_store.user.UserReactionPagingStore
import net.pantasystem.milktea.data.infrastructure.report.ReportRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.user.*
import net.pantasystem.milktea.data.infrastructure.user.block.BlockApiAdapter
import net.pantasystem.milktea.data.infrastructure.user.block.BlockApiAdapterImpl
import net.pantasystem.milktea.data.infrastructure.user.block.BlockRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.user.follow.FollowApiAdapter
import net.pantasystem.milktea.data.infrastructure.user.follow.FollowApiAdapterImpl
import net.pantasystem.milktea.data.infrastructure.user.follow.FollowRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.user.mute.MuteApiAdapter
import net.pantasystem.milktea.data.infrastructure.user.mute.MuteApiAdapterImpl
import net.pantasystem.milktea.data.infrastructure.user.mute.MuteRepositoryImpl
import net.pantasystem.milktea.model.user.FollowRequestRepository
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.block.BlockRepository
import net.pantasystem.milktea.model.user.follow.FollowRepository
import net.pantasystem.milktea.model.user.mute.MuteRepository
import net.pantasystem.milktea.model.user.report.ReportRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    @Singleton
    abstract fun userDataSource(dataSource: MediatorUserDataSource): UserDataSource

    @Binds
    @Singleton
    internal abstract fun userRepository(
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

    @Binds
    @Singleton
    internal abstract fun bindMuteRepository(impl: MuteRepositoryImpl): MuteRepository

    @Binds
    @Singleton
    internal abstract fun bindMuteApiAdapter(impl: MuteApiAdapterImpl): MuteApiAdapter

    @Binds
    @Singleton
    internal abstract fun bindBlockRepository(impl: BlockRepositoryImpl): BlockRepository

    @Binds
    @Singleton
    internal abstract fun bindBlockApiAdapter(impl: BlockApiAdapterImpl): BlockApiAdapter

    @Binds
    @Singleton
    internal abstract fun bindUserApiAdapter(impl: UserApiAdapterImpl): UserApiAdapter

    @Binds
    @Singleton
    internal abstract fun bindUserCacheUpdater(impl: UserCacheUpdaterFromUserActionResultImpl): UserCacheUpdaterFromUserActionResult

    @Binds
    @Singleton
    internal abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    internal abstract fun bindFollowRepository(impl: FollowRepositoryImpl): FollowRepository

    @Binds
    @Singleton
    internal abstract fun bindFollowApiAdapter(impl: FollowApiAdapterImpl): FollowApiAdapter
}