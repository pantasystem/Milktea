package jp.panta.misskeyandroidclient.model.sw.register

import jp.panta.misskeyandroidclient.BuildConfig
import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.sw.register.Subscription
import jp.panta.misskeyandroidclient.api.sw.register.SubscriptionState
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.instance.MetaStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class SubscriptionRegistration(
    val accountRepository: AccountRepository,
    val metaStore: MetaStore,
    val encryption: Encryption,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val deviceId: String,
    val endpointBase: String = BuildConfig.PUSH_TO_FCM_SERVER_BASE_URL,
    val auth: String = BuildConfig.PUSH_TO_FCM_AUTH,
    val publicKey: String = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY
) {

    /**
     * 特定のアカウントをsw/registerに登録します。
     */
    suspend fun register(accountId: Long) : SubscriptionState?{

        val account = accountRepository.get(accountId)
        val endpoint = "$endpointBase/web-push-handler/${deviceId}/account/${account.getI(encryption)}"

        val api = misskeyAPIProvider.get(account.instanceDomain)
        val res = api.swRegister(
            Subscription(
                i = account.getI(encryption),
                endpoint = endpoint,
                auth = auth,
                publicKey = publicKey
            )
        )
        return res.body()
    }

    /**
     * 全てのアカウントをsw/registerに登録します。
     * @return 成功件数
     */
    suspend fun registerAll() : Int{
        val accounts = accountRepository.findAll()
        return coroutineScope {
            accounts.map {
                async {
                    val result = runCatching {

                    }.getOrNull()
                    if(result == null) 0 else 1
                }
            }.awaitAll().sumOf { it }
        }
    }
}