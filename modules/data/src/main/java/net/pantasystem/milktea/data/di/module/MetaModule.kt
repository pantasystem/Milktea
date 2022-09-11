package net.pantasystem.milktea.data.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.instance.db.InMemoryMetaRepository
import net.pantasystem.milktea.data.infrastructure.instance.db.MediatorMetaRepository
import net.pantasystem.milktea.data.infrastructure.instance.db.RoomMetaRepository
import net.pantasystem.milktea.data.infrastructure.instance.remote.RemoteFetchMeta
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.instance.MediatorFetchMeta
import net.pantasystem.milktea.model.instance.MetaRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MetaModule {

    @Provides
    @Singleton
    fun metaRepository(db: DataBase): MetaRepository {
        return MediatorMetaRepository(
            RoomMetaRepository(
                db.metaDAO(),
                db.emojiAliasDAO(),
                db,
            ),
            InMemoryMetaRepository()
        )
    }

    @Provides
    @Singleton
    fun metaStore(
        metaRepository: MetaRepository,
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