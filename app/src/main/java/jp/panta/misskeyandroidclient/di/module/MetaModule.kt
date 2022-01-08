package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.instance.MediatorMetaStore
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import jp.panta.misskeyandroidclient.model.instance.MetaStore
import jp.panta.misskeyandroidclient.model.instance.db.InMemoryMetaRepository
import jp.panta.misskeyandroidclient.model.instance.db.MediatorMetaRepository
import jp.panta.misskeyandroidclient.model.instance.db.RoomMetaRepository
import jp.panta.misskeyandroidclient.model.instance.remote.RemoteMetaStore
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
        loggerFactory: Logger.Factory
    ): MetaStore {
        return MediatorMetaStore(
            metaRepository,
            RemoteMetaStore(),
            loggerFactory
        )
    }
}