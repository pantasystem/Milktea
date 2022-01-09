package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notes.impl.InMemoryNoteDataSource
import jp.panta.misskeyandroidclient.model.notes.impl.NoteRepositoryImpl
import jp.panta.misskeyandroidclient.model.notes.reservation.AndroidNoteReservationPostExecutor
import jp.panta.misskeyandroidclient.model.notes.reservation.NoteReservationPostExecutor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteBindModule{

    @Binds
    @Singleton
    abstract fun noteDataSource(inMem: InMemoryNoteDataSource): NoteDataSource

    @Binds
    @Singleton
    abstract fun noteRepository(impl: NoteRepositoryImpl): NoteRepository
}


@Module
@InstallIn(SingletonComponent::class)
object NoteModule {
    @Provides
    @Singleton
    fun noteReservationPostExecutor(
        @ApplicationContext context: Context
    ) : NoteReservationPostExecutor {
        return AndroidNoteReservationPostExecutor(context)
    }
}