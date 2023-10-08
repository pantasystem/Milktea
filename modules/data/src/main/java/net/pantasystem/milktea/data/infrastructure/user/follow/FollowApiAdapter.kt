package net.pantasystem.milktea.data.infrastructure.user.follow

import net.pantasystem.milktea.api.mastodon.accounts.FollowParamsRequest
import net.pantasystem.milktea.api.misskey.users.CancelFollow
import net.pantasystem.milktea.api.misskey.users.follow.FollowUserRequest
import net.pantasystem.milktea.api.misskey.users.follow.UnFollowUserRequest
import net.pantasystem.milktea.api.misskey.users.follow.UpdateUserFollowRequest
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.user.UserActionResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.follow.FollowUpdateParams
import javax.inject.Inject

internal interface FollowApiAdapter {

    suspend fun follow(userId: User.Id): UserActionResult

    suspend fun cancelFollowRequest(userId: User.Id): UserActionResult

    suspend fun unfollow(userId: User.Id): UserActionResult

    suspend fun update(userId: User.Id, params: FollowUpdateParams): UserActionResult

}

internal class FollowApiAdapterImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
) : FollowApiAdapter {
    override suspend fun follow(userId: User.Id): UserActionResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                misskeyAPIProvider.get(account).followUser(
                    FollowUserRequest(userId = userId.id, i = account.token)
                ).throwIfHasError()
                UserActionResult.Misskey
            }

            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                mastodonAPIProvider.get(account).follow(userId.id, FollowParamsRequest())
                    .throwIfHasError().body().let {
                        UserActionResult.Mastodon(requireNotNull(it))
                    }
            }
        }
    }

    override suspend fun unfollow(userId: User.Id): UserActionResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                misskeyAPIProvider.get(account)
                    .unFollowUser(UnFollowUserRequest(userId = userId.id, i = account.token))
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
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
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

    override suspend fun update(userId: User.Id, params: FollowUpdateParams): UserActionResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                mastodonAPIProvider.get(account).follow(
                    userId.id, FollowParamsRequest(
//                    reblogs = params.isReblog,
                        notify = params.isNotify
                    )
                ).throwIfHasError()
                    .body().let {
                        UserActionResult.Mastodon(requireNotNull(it))
                    }
            }

            Account.InstanceType.FIREFISH, Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).updateFollowUser(
                    UpdateUserFollowRequest(
                        i = account.token,
                        userId = userId.id,
                        notify = params.isNotify?.let {
                            if (it) "normal" else "none"
                        },
//                        withReplies = params.withReplies,
                    )
                ).throwIfHasError()
                UserActionResult.Misskey
            }
        }
    }
}