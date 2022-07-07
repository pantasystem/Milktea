package net.pantasystem.milktea.data.infrastructure.sw.register

import net.pantasystem.milktea.api.misskey.register.UnSubscription
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Singleton

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
        val account = accountRepository.get(accountId).getOrThrow()
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