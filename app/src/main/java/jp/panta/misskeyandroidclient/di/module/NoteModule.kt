package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.AndroidNoteReservationPostExecutor
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.draft.DraftNoteRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.notes.NoteStreamingImpl
import net.pantasystem.milktea.data.infrastructure.notes.NoteTranslationStoreImpl
import net.pantasystem.milktea.data.infrastructure.notes.TimelineStoreImpl
import net.pantasystem.milktea.data.infrastructure.notes.impl.DraftNoteServiceImpl
import net.pantasystem.milktea.data.infrastructure.notes.impl.InMemoryNoteDataSource
import net.pantasystem.milktea.data.infrastructure.notes.impl.NoteRepositoryImpl
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.draft.DraftNoteService
import net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor
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

    @Binds
    @Singleton
    abstract fun provideTimelineStoreFactory(impl: TimelineStoreImpl.Factory): TimelineStore.Factory

    @Binds
    @Singleton
    abstract fun provideNoteStreaming(impl: NoteStreamingImpl): NoteStreaming

    @Binds
    @Singleton
    abstract fun provideDraftNoteRepository(impl: DraftNoteRepositoryImpl): DraftNoteRepository

    @Binds
    @Singleton
    abstract fun provideDraftNoteService(impl: DraftNoteServiceImpl): DraftNoteService
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

@Module
@InstallIn(SingletonComponent::class)
abstract class AbsNoteModule {
    @Binds
    @Singleton
    abstract fun provideNoteTranslationStore(
        impl: NoteTranslationStoreImpl
    ) : NoteTranslationStore
}