package jp.panta.misskeyandroidclient.model.sw.register

import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.misskey.sw.register.UnSubscription
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.AccountRepository

class SubscriptionUnRegistration(
    val accountRepository: AccountRepository,
    val encryption: Encryption,
    val lang: String,
    val misskeyAPIProvider: MisskeyAPIProvider
) {

    suspend fun unregister(deviceToken: String, accountId: Long) {
        val account = accountRepository.get(accountId)
        val apiProvider = misskeyAPIProvider.get(account)
        val endpoint = EndpointBuilder(
            accountId = account.accountId,
            deviceToken = deviceToken,
            lang = lang
        ).build()
        apiProvider.swUnRegister(
            UnSubscription(
            i = account.getI(encryption),
            endpoint = endpoint
        )
        ).throwIfHasError()
    }
}