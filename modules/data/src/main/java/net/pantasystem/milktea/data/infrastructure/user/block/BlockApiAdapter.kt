package net.pantasystem.milktea.data.infrastructure.user.block

import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.user.UserActionResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

internal interface BlockApiAdapter {

    suspend fun blockUser(userId: User.Id): BlockUserResult
    suspend fun unblockUser(userId: User.Id): UnBlockUserResult
}

class BlockApiAdapterImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider
) : BlockApiAdapter {
    override suspend fun blockUser(userId: User.Id): BlockUserResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).blockUser(
                    RequestUser(
                        i = account.token,
                        userId = userId.id,
                    )
                )
                    .throwIfHasError()
                UserActionResult.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val body = mastodonAPIProvider.get(account).blockAccount(userId.id)
                    .throwIfHasError()
                    .body()
                UserActionResult.Mastodon(requireNotNull(body))
            }
        }
    }

    override suspend fun unblockUser(userId: User.Id): UnBlockUserResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).unblockUser(
                    RequestUser(
                        i = account.token,
                        userId = userId.id,
                    )
                )
                    .throwIfHasError()
                UserActionResult.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val body = mastodonAPIProvider.get(account).unblockAccount(userId.id)
                    .throwIfHasError()
                    .body()
                UserActionResult.Mastodon(requireNotNull(body))
            }
        }
    }

}
typealias BlockUserResult = UserActionResult

typealias UnBlockUserResult = UserActionResult