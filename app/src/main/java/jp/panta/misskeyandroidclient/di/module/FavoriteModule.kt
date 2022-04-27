package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.notes.favorite.FavoriteRepositoryImpl
import net.pantasystem.milktea.model.notes.favorite.FavoriteRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoriteModule {

    @Binds
    @Provides
    abstract fun provideFavoriteRepository(
        impl: FavoriteRepositoryImpl
    ): FavoriteRepository
}