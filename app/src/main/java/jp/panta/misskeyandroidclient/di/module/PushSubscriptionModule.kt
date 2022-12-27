package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.BuildConfig
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistrationImpl
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionUnRegistrationImpl
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.model.sw.register.SubscriptionUnRegistration
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PushSubscriptionModule {

    @Singleton
    @Provides
    fun provideSubscriptionRegistration(
        @ApplicationContext context: Context,
        accountRepository: AccountRepository,
        misskeyAPIProvider: MisskeyAPIProvider,
        loggerFactory: Logger.Factory
    ): SubscriptionRegistration {
        return SubscriptionRegistrationImpl(
            accountRepository,
            misskeyAPIProvider,
            lang = Locale.getDefault().language,
            loggerFactory,
            auth = BuildConfig.PUSH_TO_FCM_AUTH,
            publicKey = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY,
            endpointBase = BuildConfig.PUSH_TO_FCM_SERVER_BASE_URL,
            context = context,
        )
    }

    @Singleton
    @Provides
    fun provideUnSubscriptionRegistration(
        @ApplicationContext context: Context,
        accountRepository: AccountRepository,
        misskeyAPIProvider: MisskeyAPIProvider,
    ): SubscriptionUnRegistration {
        return SubscriptionUnRegistrationImpl(
            accountRepository,
            lang = Locale.getDefault().language,
            misskeyAPIProvider = misskeyAPIProvider,
            endpointBase = BuildConfig.PUSH_TO_FCM_SERVER_BASE_URL,
            auth = BuildConfig.PUSH_TO_FCM_AUTH,
            publicKey = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY,
            context = context,
        )
    }
}