package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.AndroidNoteReservationPostExecutor
import net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor
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
