package net.pantasystem.milktea.data.infrastructure.instance.online.user.count

import net.pantasystem.milktea.api.misskey.EmptyRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.instance.online.user.count.OnlineUserCountRepository
import net.pantasystem.milktea.model.instance.online.user.count.OnlineUserCountResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineUserCountRepositoryImpl @Inject constructor(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
): OnlineUserCountRepository {
    override suspend fun find(accountId: Long): Result<OnlineUserCountResult> = runCancellableCatching {
        val account = accountRepository.get(accountId).getOrThrow()
        when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                val res = misskeyAPIProvider.get(account).getOnlineUsersCount(EmptyRequest)
                    .throwIfHasError().body()
                OnlineUserCountResult.Success(requireNotNull(res?.count))
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> OnlineUserCountResult.Unknown
        }
    }
}