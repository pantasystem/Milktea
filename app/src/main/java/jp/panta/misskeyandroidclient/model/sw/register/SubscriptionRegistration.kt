package jp.panta.misskeyandroidclient.model.sw.register

import jp.panta.misskeyandroidclient.BuildConfig
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.misskey.sw.register.Subscription
import jp.panta.misskeyandroidclient.api.misskey.sw.register.SubscriptionState
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SubscriptionRegistration(
    val accountRepository: AccountRepository,
    val encryption: Encryption,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val lang: String,
    loggerFactory: Logger.Factory,
    val auth: String = BuildConfig.PUSH_TO_FCM_AUTH,
    private val publicKey: String = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY
) {
    val logger = loggerFactory.create("sw/register")

    /**
     * 特定のアカウントをsw/registerに登録します。
     */
    private suspend fun register(deviceToken: String, accountId: Long) : SubscriptionState?{

        logger.debug("call register(accountId:$accountId)")
        val account = accountRepository.get(accountId)
        val endpoint = EndpointBuilder(
            deviceToken = deviceToken,
            accountId = accountId,
            lang = lang
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