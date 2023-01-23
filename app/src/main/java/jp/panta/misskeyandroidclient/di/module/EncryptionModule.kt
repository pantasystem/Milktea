package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.infrastructure.auth.KeyStoreSystemEncryption
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {

    @Provides
    @Singleton
    fun encryption(@ApplicationContext context: Context): Encryption {
        return KeyStoreSystemEncryption(context)
    }
}