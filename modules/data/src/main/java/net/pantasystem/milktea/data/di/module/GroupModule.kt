package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.group.GroupDataSourceImpl
import net.pantasystem.milktea.data.infrastructure.group.GroupRepositoryImpl
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.group.GroupRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GroupModule {
    @Binds
    @Singleton
    abstract fun groupDataSource(ds: GroupDataSourceImpl) : GroupDataSource

    @Binds
    @Singleton
    abstract fun groupRepository(ds: GroupRepositoryImpl) : GroupRepository
}