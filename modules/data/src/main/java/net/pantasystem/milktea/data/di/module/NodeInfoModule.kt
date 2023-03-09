package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.api.NodeInfoAPIBuilder
import net.pantasystem.milktea.data.api.NodeInfoAPIBuilderImpl
import net.pantasystem.milktea.data.infrastructure.nodeinfo.NodeInfoRepositoryImpl
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NodeInfoModule {

    @Binds
    @Singleton
    abstract fun bindNodeInfoRepository(impl: NodeInfoRepositoryImpl): NodeInfoRepository

    @Binds
    @Singleton
    abstract fun nodeInfoAPIBuilder(impl: NodeInfoAPIBuilderImpl): NodeInfoAPIBuilder

}