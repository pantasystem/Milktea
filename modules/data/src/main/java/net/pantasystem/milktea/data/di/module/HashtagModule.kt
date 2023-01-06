package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.hashtag.HashtagRepositoryImpl
import net.pantasystem.milktea.model.hashtag.HashtagRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HashtagBindModule {

    @Binds
    @Singleton
    abstract fun bindHashtagRepository(impl: HashtagRepositoryImpl): HashtagRepository
}