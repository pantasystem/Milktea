package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.common.Logger
import jp.panta.misskeyandroidclient.api.misskey.logger.AndroidDefaultLogger
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppLoggerModule {
    @Singleton
    @Provides
    fun loggerFactory(): net.pantasystem.milktea.common.Logger.Factory {
        return AndroidDefaultLogger.Factory
    }
}