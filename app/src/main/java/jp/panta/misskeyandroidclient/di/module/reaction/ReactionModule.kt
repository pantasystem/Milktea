package jp.panta.misskeyandroidclient.di.module.reaction

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
import jp.panta.misskeyandroidclient.model.notes.reaction.impl.InMemoryReactionHistoryDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class ReactionModule {

    @Binds
    abstract fun reactionHistoryDataSource(ds: InMemoryReactionHistoryDataSource): ReactionHistoryDataSource
}