package net.pantasystem.milktea.data.infrastructure.sw.register

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.api.misskey.register.Subscription
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.sw.register.DeviceTokenRepository
import net.pantasystem.milktea.model.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.model.sw.register.SubscriptionState

class SubscriptionRegistrationImpl(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val lang: String,
    loggerFactory: Logger.Factory,
    val auth: String,
    val publicKey: String,
    val endpointBase: String,
    val context: Context,
    val deviceTokenRepository: DeviceTokenRepository,
) : SubscriptionRegistration {
    val logger = loggerFactory.create("sw/register")

    /**
     * 特定のアカウントをsw/registerに登録します。
     */
    override suspend fun register(accountId: Long) : Result<SubscriptionState?> = runCancellableCatching {
        val token = deviceTokenRepository.getOrCreate().getOrThrow()
        logger.debug("call register(accountId:$accountId)")
        logger.debug("auth:$auth, publicKey:$publicKey")
        val account = accountRepository.get(accountId).getOrThrow()
        val endpoint = EndpointBuilder(
            deviceToken = token,
            accountId = accountId,
            lang = lang,
            auth = auth,
            endpointBase = endpointBase,
            publicKey = publicKey
        ).build()
        logger.debug("endpoint:${endpoint}")

        val api = misskeyAPIProvider.get(account.normalizedInstanceUri)
        val res = api.swRegister(
            Subscription(
                i = account.token,
                endpoint = endpoint,
                auth = auth,
                publicKey = publicKey
            )
        )
        res.throwIfHasError()
        logger.debug("res code:${res.code()}, body:${res.body()}")
        res.body()?.let {
            SubscriptionState(
                state = it.state,
                key = it.key
            )
        }

    }

    /**
     * 全てのアカウントをsw/registerに登録します。
     * @return 成功件数
     */
    override suspend fun registerAll() : Int {
        val accounts = accountRepository.findAll().getOrThrow()
        return coroutineScope {
            accounts.map {
                async {
                    register(it.accountId).onFailure {
                        logger.error("sw/registerに失敗しました", it)
                    }.onSuccess { result ->
                        logger.debug("subscription result:${result}")
                    }.fold(
                        onSuccess = {
                            1
                        },
                        onFailure = {
                            0
                        }
                    )
                }
            }.awaitAll().sumOf { it }
        }
    }
}