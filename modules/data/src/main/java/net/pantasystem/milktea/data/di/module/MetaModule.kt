package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.instance.MetaRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.instance.db.InMemoryMetaDataSource
import net.pantasystem.milktea.data.infrastructure.instance.db.MediatorMetaDataSource
import net.pantasystem.milktea.data.infrastructure.instance.db.RoomMetaDataSource
import net.pantasystem.milktea.model.instance.MetaDataSource
import net.pantasystem.milktea.model.instance.MetaRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MetaModule {

    @Provides
    @Singleton
    fun provideMetaDataSource(db: DataBase): MetaDataSource {
        return MediatorMetaDataSource(
            RoomMetaDataSource(
                db.metaDAO(),
                db.emojiAliasDAO(),
                db,
            ),
            InMemoryMetaDataSource()
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