package net.pantasystem.milktea.data.infrastructure.sw.register

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.api.misskey.register.Subscription
import net.pantasystem.milktea.api.misskey.register.SubscriptionState
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountRepository

class SubscriptionRegistration(
    val accountRepository: AccountRepository,
    val encryption: Encryption,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val lang: String,
    loggerFactory: Logger.Factory,
    val auth: String,
    private val publicKey: String,
    private val endpointBase: String,
) {
    val logger = loggerFactory.create("sw/register")

    /**
     * 特定のアカウントをsw/registerに登録します。
     */
    private suspend fun register(deviceToken: String, accountId: Long) : SubscriptionState?{

        logger.debug("call register(accountId:$accountId)")
        val account = accountRepository.get(accountId).getOrThrow()
        val endpoint = EndpointBuilder(
            deviceToken = deviceToken,
            accountId = accountId,
            lang = lang,
            auth = auth,
            endpointBase = endpointBase,
            publicKey = publicKey
        ).build()
        logger.debug("endpoint:${endpoint}")

        val api = misskeyAPIProvider.get(account.instanceDomain)
        val res = api.swRegister(
            Subscription(
                i = account.getI(encryption),
                endpoint = endpoint,
                auth = auth,
                publicKey = publicKey
            )
        )
        res.throwIfHasError()
        logger.debug("res code:${res.code()}, body:${res.body()}")
        return res.body()
    }

    /**
     * 全てのアカウントをsw/registerに登録します。
     * @return 成功件数
     */
    suspend fun registerAll(deviceToken: String) : Int{
        val accounts = accountRepository.findAll()
        return coroutineScope {
            accounts.map {
                async {
                    val result: SubscriptionState? = runCatching {
                        register(deviceToken, it.accountId)
                    }.onFailure {
                        logger.error("sw/registerに失敗しました", it)
                    }.getOrNull()
                    logger.debug("subscription result:${result}")
                    if(result == null) 0 else 1
                }
            }.awaitAll().sumOf { it }
        }
    }
}