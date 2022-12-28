package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.instance.FeatureEnablesImpl
import net.pantasystem.milktea.data.infrastructure.instance.InstanceInfoRepositoryImpl
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.InstanceInfoRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class InstanceInfoBindModule {

    @Binds
    @Singleton
    abstract fun bindInstanceInfoRepository(impl: InstanceInfoRepositoryImpl): InstanceInfoRepository

    @Binds
    @Singleton
    abstract fun bindFeatureEnables(impl: FeatureEnablesImpl): FeatureEnables
}