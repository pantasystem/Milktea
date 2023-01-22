package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.api.mastodon.accounts.MuteAccountRequest
import net.pantasystem.milktea.api.misskey.users.CreateMuteUserRequest
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.data.infrastructure.toUserRelated
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.mute.CreateMute
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApiAdapter @Inject constructor(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
){

    suspend fun show(userId: User.Id, detail: Boolean): User {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                val res = misskeyAPIProvider.get(account).showUser(
                    RequestUser(
                        i = account.token,
                        userId = userId.id,
                        detail = detail,
                    )
                )
                requireNotNull(res.throwIfHasError().body()).toUser(account, detail)
            }
            Account.InstanceType.MASTODON -> {
                val res = mastodonAPIProvider.get(account).getAccount(userId.id)
                    .throwIfHasError()
                    .body()
                requireNotNull(res)
                if (detail) {
                    val relationship = mastodonAPIProvider.get(account).getAccountRelationships(
                        listOf(res.id)
                    ).throwIfHasError().body()
                    val related = requireNotNull(relationship).first().toUserRelated()
                    res.toModel(account, related)
                } else {
                    res.toModel(account)
                }
            }
        }
    }

    suspend fun follow(userId: User.Id): Boolean {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).followUser(
                    RequestUser(userId = userId.id, i = account.token)
                )
            }
            Account.InstanceType.MASTODON -> {
                mastodonAPIProvider.get(account).follow(userId.id)
            }
        }.throwIfHasError().isSuccessful
    }

    suspend fun unfollow(userId: User.Id): Boolean {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> misskeyAPIProvider.get(account)
                .unFollowUser(RequestUser(userId = userId.id, i = account.token))
                .throwIfHasError()
                .isSuccessful
            Account.InstanceType.MASTODON -> mastodonAPIProvider.get(account).unfollow(userId.id)
                .throwIfHasError()
                .isSuccessful
        }
    }

    suspend fun muteUser(createMute: CreateMute): MuteUserResult {
        val account = accountRepository.get(createMute.userId.accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                require(createMute.notifications == null) {
                    "Misskey does not support notifications mute account parameter"
                }
                misskeyAPIProvider.get(account).muteUser(
                    CreateMuteUserRequest(
                        i = account.token,
                        userId = createMute.userId.id,
                        expiresAt = createMute.expiresAt?.toEpochMilliseconds()
                    )
                ).throwIfHasError()
                MuteUserResult.Misskey
            }
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account).muteAccount(
                    createMute.userId.id,
                    MuteAccountRequest(
                        duration = createMute.expiresAt?.let {
                            Clock.System.now().epochSeconds - it.epochSeconds
                        } ?: 0,
                        notifications = createMute.notifications ?: true
                    )
                ).throwIfHasError().body()
                MuteUserResult.Mastodon(requireNotNull(body))
            }
        }
    }
}

sealed interface MuteUserResult {
    object Misskey : MuteUserResult
    data class Mastodon(val relationship: MastodonAccountRelationshipDTO) : MuteUserResult
}