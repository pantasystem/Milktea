package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.objectbox.BoxStore
import net.pantasystem.milktea.data.infrastructure.notes.impl.db.MyObjectBox
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ObjectBoxModule {

    @Provides
    @Singleton
    fun provideObjectBoxStore(
        @ApplicationContext context: Context
    ): BoxStore {
        return MyObjectBox.builder().maxReaders(1000).androidContext(context).build()
    }
}