package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.instance.MetaRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.instance.db.InMemoryMetaDataSource
import net.pantasystem.milktea.data.infrastructure.instance.db.MediatorMetaDataSource
import net.pantasystem.milktea.data.infrastructure.instance.db.RoomMetaDataSource
import net.pantasystem.milktea.data.infrastructure.instance.remote.RemoteFetchMeta
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.instance.MediatorFetchMeta
import net.pantasystem.milktea.model.instance.MetaDataSource
import net.pantasystem.milktea.model.instance.MetaRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MetaModule {

    @Provides
    @Singleton
    fun metaRepository(db: DataBase): MetaDataSource {
        return MediatorMetaDataSource(
            RoomMetaDataSource(
                db.metaDAO(),
                db.emojiAliasDAO(),
                db,
            ),
            InMemoryMetaDataSource()
        )
    }

    @Provides
    @Singleton
    fun metaStore(
        metaRepository: MetaDataSource,
        loggerFactory: Logger.Factory,
        misskeyAPIProvider: MisskeyAPIProvider,
    ): FetchMeta {
        return MediatorFetchMeta(
            metaRepository,
            RemoteFetchMeta(misskeyAPIProvider),
            loggerFactory
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MetaBindModule {
    @Binds
    @Singleton
    abstract fun bindMetaRepository(impl: MetaRepositoryImpl): MetaRepository
}