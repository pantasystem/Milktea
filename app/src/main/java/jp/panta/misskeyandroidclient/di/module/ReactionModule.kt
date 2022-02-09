package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
import jp.panta.misskeyandroidclient.model.notes.reaction.impl.InMemoryReactionHistoryDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReactionModule {

    @Binds
    @Singleton
    abstract fun reactionHistoryDataSource(ds: InMemoryReactionHistoryDataSource): ReactionHistoryDataSource
}