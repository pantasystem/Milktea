package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.instance.FeatureEnablesImpl
import net.pantasystem.milktea.data.infrastructure.instance.online.user.count.OnlineUserCountRepositoryImpl
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.online.user.count.OnlineUserCountRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class InstanceInfoBindModule {

//    @Binds
//    @Singleton
//    abstract fun bindInstanceInfoRepository(impl: InstanceInfoRepositoryImpl): InstanceInfoRepository

    @Binds
    @Singleton
    abstract fun bindFeatureEnables(impl: FeatureEnablesImpl): FeatureEnables

    @Binds
    @Singleton
    abstract fun bindOnlineUserCountRepository(impl: OnlineUserCountRepositoryImpl): OnlineUserCountRepository
}