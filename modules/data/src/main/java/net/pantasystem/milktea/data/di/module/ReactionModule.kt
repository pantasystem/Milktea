package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.emoji.UserEmojiConfigRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.InMemoryReactionHistoryDataSource
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.ReactionHistoryPaginatorImpl
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.ReactionUserRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history.ReactionHistoryRepositoryImpl
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reaction.ReactionUserRepository
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReactionModule {

    @Binds
    @Singleton
    abstract fun bindReactionHistoryDataSource(ds: InMemoryReactionHistoryDataSource): ReactionHistoryDataSource

    @Binds
    @Singleton
    abstract fun bindReactionHistoryPaging(impl: ReactionHistoryPaginatorImpl.Factory): ReactionHistoryPaginator.Factory

    @Binds
    @Singleton
    abstract fun bindReactionHistoryRepository(impl: ReactionHistoryRepositoryImpl): ReactionHistoryRepository

    @Binds
    @Singleton
    abstract fun bindUserEmojiConfigRepository(impl: UserEmojiConfigRepositoryImpl): UserEmojiConfigRepository

    @Binds
    @Singleton
    abstract fun bindReactionUserRepository(impl: ReactionUserRepositoryImpl): ReactionUserRepository
}