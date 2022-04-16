package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.model.group.GroupDataSource
import net.pantasystem.milktea.data.model.group.GroupRepository
import net.pantasystem.milktea.data.model.group.impl.GroupRepositoryImpl
import net.pantasystem.milktea.data.model.group.impl.InMemoryGroupDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GroupModule {
    @Binds
    @Singleton
    abstract fun groupDataSource(ds: InMemoryGroupDataSource) : GroupDataSource

    @Binds
    @Singleton
    abstract fun groupRepository(ds: GroupRepositoryImpl) : GroupRepository
}