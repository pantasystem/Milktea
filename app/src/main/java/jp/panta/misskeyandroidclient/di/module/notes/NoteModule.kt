package jp.panta.misskeyandroidclient.di.module.notes

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notes.impl.InMemoryNoteDataSource
import jp.panta.misskeyandroidclient.model.notes.impl.NoteRepositoryImpl
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.model.users.impl.InMemoryUserDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteModule{

    @Binds
    abstract fun noteDataSource(inMem: InMemoryNoteDataSource): NoteDataSource

    @Binds
    abstract fun noteRepository(impl: NoteRepositoryImpl): NoteRepository
}