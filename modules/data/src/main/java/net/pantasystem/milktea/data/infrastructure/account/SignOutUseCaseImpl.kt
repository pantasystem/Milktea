package net.pantasystem.milktea.data.infrastructure.account

import net.pantasystem.milktea.api_streaming.network.SocketImpl
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.SignOutUseCase
import net.pantasystem.milktea.model.sw.register.SubscriptionUnRegistration
import javax.inject.Inject


class SignOutUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val subscriptionUnRegistration: SubscriptionUnRegistration,
    private val socketWithAccountProvider: SocketWithAccountProvider,
    private val accountStore: AccountStore,
): SignOutUseCase {

    override suspend fun invoke(account: Account): Result<Unit> {
        return runCancellableCatching {
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    subscriptionUnRegistration
                        .unregister(account.accountId)
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {}
            }
        }.mapCancellableCatching {
            accountRepository.delete(account)
        }.mapCancellableCatching {
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    val socket = socketWithAccountProvider.get(account.accountId)
                    if (socket is SocketImpl) {
                        socket.destroy()
                    } else {
                        socketWithAccountProvider.get(account.accountId)?.disconnect()
                    }
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {}
            }
        }.mapCancellableCatching {
            accountStore.initialize()
        }
    }
}