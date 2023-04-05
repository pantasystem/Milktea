package net.pantasystem.milktea.data.infrastructure.user.follow

import net.pantasystem.milktea.api.misskey.users.CancelFollow
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.user.UserActionResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

internal interface FollowApiAdapter {

    suspend fun follow(userId: User.Id): UserActionResult

    suspend fun cancelFollowRequest(userId: User.Id): UserActionResult

    suspend fun unfollow(userId: User.Id): UserActionResult

}

internal class FollowApiAdapterImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
) : FollowApiAdapter {
    override suspend fun follow(userId: User.Id): UserActionResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).followUser(
                    RequestUser(userId = userId.id, i = account.token)
                ).throwIfHasError()
                UserActionResult.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                mastodonAPIProvider.get(account).follow(userId.id)
                    .throwIfHasError().body().let {
                        UserActionResult.Mastodon(requireNotNull(it))
                    }
            }
        }
    }

    override suspend fun unfollow(userId: User.Id): UserActionResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account)
                    .unFollowUser(RequestUser(userId = userId.id, i = account.token))
                    .throwIfHasError()
                    .body()
                UserActionResult.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                mastodonAPIProvider.get(account).unfollow(userId.id)
                    .throwIfHasError()
                    .body().let {
                        UserActionResult.Mastodon(requireNotNull(it))
                    }

            }
        }
    }

    override suspend fun cancelFollowRequest(userId: User.Id): UserActionResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).cancelFollowRequest(
                    CancelFollow(
                        i = account.token,
                        userId = userId.id
                    )
                ).throwIfHasError()
                UserActionResult.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                mastodonAPIProvider.get(account).unfollow(userId.id).throwIfHasError()
                    .body().let {
                        UserActionResult.Mastodon(requireNotNull(it))
                    }
            }
        }
    }
}