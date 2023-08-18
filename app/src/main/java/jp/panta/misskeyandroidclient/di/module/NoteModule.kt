package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.AndroidNoteReservationPostExecutor
import net.pantasystem.milktea.common_android_ui.UserPinnedNotesFragmentFactory
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.ReactionRepositoryImpl
import net.pantasystem.milktea.model.note.reaction.ReactionRepository
import net.pantasystem.milktea.model.note.reservation.NoteReservationPostExecutor
import net.pantasystem.milktea.note.pinned.UserPinnedNotesFragmentFactoryImpl
import javax.inject.Singleton

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
abstract class NoteBindModule {
    @Binds
    @Singleton
    abstract fun bindUserPinnedNotesFragmentFactory(impl: UserPinnedNotesFragmentFactoryImpl): UserPinnedNotesFragmentFactory

    @Binds
    @Singleton
    abstract fun bindReactionRepository(impl: ReactionRepositoryImpl): ReactionRepository
}
