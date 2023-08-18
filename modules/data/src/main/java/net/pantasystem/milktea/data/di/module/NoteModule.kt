package net.pantasystem.milktea.data.di.module

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.app_store.notes.TimelineStore
import net.pantasystem.milktea.common.getPreferences
import net.pantasystem.milktea.data.infrastructure.notes.*
import net.pantasystem.milktea.data.infrastructure.notes.draft.DraftNoteRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.notes.impl.DraftNoteServiceImpl
import net.pantasystem.milktea.data.infrastructure.notes.impl.NoteApiAdapter
import net.pantasystem.milktea.data.infrastructure.notes.impl.NoteApiAdapterFactoryImpl
import net.pantasystem.milktea.data.infrastructure.notes.impl.NoteRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.notes.impl.ObjectBoxNoteDataSource
import net.pantasystem.milktea.data.infrastructure.notes.impl.ThreadContextApiAdapter
import net.pantasystem.milktea.data.infrastructure.notes.impl.ThreadContextApiAdapterFactoryImpl
import net.pantasystem.milktea.data.infrastructure.notes.renote.RenotesPagingServiceImpl
import net.pantasystem.milktea.model.note.*
import net.pantasystem.milktea.model.note.draft.DraftNoteRepository
import net.pantasystem.milktea.model.note.draft.DraftNoteService
import net.pantasystem.milktea.model.note.repost.RenotesPagingService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteBindModule{

    @Binds
    @Singleton
    abstract fun noteDataSource(inMem: ObjectBoxNoteDataSource): NoteDataSource

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
    abstract fun provideDraftNoteService(impl: DraftNoteServiceImpl): DraftNoteService

    @Binds
    @Singleton
    abstract fun bindRenotePagingService(impl: RenotesPagingServiceImpl.Factory): RenotesPagingService.Factory

    @Binds
    @Singleton
    abstract fun provideDraftNoteRepository(impl: DraftNoteRepositoryImpl): DraftNoteRepository

    @Binds
    @Singleton
    abstract fun bindReplyStreaming(impl: ReplyStreamingImpl): ReplyStreaming

    @Binds
    internal abstract fun bindNoteApiAdapterFactory(impl: NoteApiAdapterFactoryImpl): NoteApiAdapter.Factory

    @Binds
    internal abstract fun bindThreadContextApiAdapterFactory(impl: ThreadContextApiAdapterFactoryImpl): ThreadContextApiAdapter.Factory

}

@Module
@InstallIn(SingletonComponent::class)
object NoteProvideModule {
    @Provides
    @Singleton
    fun provideTimelineScrollPositionRepository(
        @ApplicationContext context: Context
    ): TimelineScrollPositionRepository {
        return TimelineScrollPositionRepositoryImpl(
            context.getPreferences()
        )
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