package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.note.wordmute.WordFilterConfigRepositoryImpl
import net.pantasystem.milktea.model.note.muteword.WordFilterConfigRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WordFilterConfigModule {

    @Binds
    @Singleton
    abstract fun bindWordFilterConfigRepository(impl: WordFilterConfigRepositoryImpl): WordFilterConfigRepository


}