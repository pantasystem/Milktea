package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.BuildConfig
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionUnRegistration
import net.pantasystem.milktea.model.account.AccountRepository
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PushSubscriptionModule {

    @Singleton
    @Provides
    fun provideSubscriptionRegistration(
        accountRepository: AccountRepository,
        encryption: Encryption,
        misskeyAPIProvider: MisskeyAPIProvider,
        loggerFactory: Logger.Factory
    ): SubscriptionRegistration {
        return SubscriptionRegistration(
            accountRepository,
            encryption,
            misskeyAPIProvider,
            lang = Locale.getDefault().language,
            loggerFactory,
            auth = BuildConfig.PUSH_TO_FCM_AUTH,
            publicKey = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY,
            endpointBase = BuildConfig.PUSH_TO_FCM_SERVER_BASE_URL,
        )
    }

    @Singleton
    @Provides
    fun provideUnSubscriptionRegistration(
        accountRepository: AccountRepository,
        encryption: Encryption,
        misskeyAPIProvider: MisskeyAPIProvider,
    ): SubscriptionUnRegistration {
        return SubscriptionUnRegistration(
            accountRepository,
            encryption,
            lang = Locale.getDefault().language,
            misskeyAPIProvider = misskeyAPIProvider,
            endpointBase = BuildConfig.PUSH_TO_FCM_SERVER_BASE_URL,
            auth = BuildConfig.PUSH_TO_FCM_AUTH,
            publicKey = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY,
        )
    }
}