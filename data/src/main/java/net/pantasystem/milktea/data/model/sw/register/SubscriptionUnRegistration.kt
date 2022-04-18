package net.pantasystem.milktea.data.model.sw.register

import net.pantasystem.milktea.api.misskey.register.UnSubscription
import net.pantasystem.milktea.api.misskey.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.model.account.AccountRepository

class SubscriptionUnRegistration(
    val accountRepository: AccountRepository,
    val encryption: Encryption,
    val lang: String,
    val misskeyAPIProvider: MisskeyAPIProvider,
    private val publicKey: String,
    private val auth: String,
    private val endpointBase: String,
) {

    suspend fun unregister(deviceToken: String, accountId: Long) {
        val account = accountRepository.get(accountId)
        val apiProvider = misskeyAPIProvider.get(account)
        val endpoint = EndpointBuilder(
            accountId = account.accountId,
            deviceToken = deviceToken,
            lang = lang,
            publicKey = publicKey,
            endpointBase = endpointBase,
            auth = auth,
        ).build()
        apiProvider.swUnRegister(
            UnSubscription(
            i = account.getI(encryption),
            endpoint = endpoint
        )
        ).throwIfHasError()
    }
}