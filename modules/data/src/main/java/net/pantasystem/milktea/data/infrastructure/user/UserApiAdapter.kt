package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.api.mastodon.accounts.MuteAccountRequest
import net.pantasystem.milktea.api.misskey.users.CreateMuteUserRequest
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.api.misskey.users.SearchByUserAndHost
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.toUserRelated
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.mute.CreateMute
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApiAdapter @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val userDTOEntityConverter: UserDTOEntityConverter,
) {

    suspend fun show(userId: User.Id, detail: Boolean): User {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                val res = misskeyAPIProvider.get(account).showUser(
                    RequestUser(
                        i = account.token,
                        userId = userId.id,
                        detail = detail,
                    )
                )
                userDTOEntityConverter.convert(
                    account,
                    requireNotNull(res.throwIfHasError().body()),
                    detail
                )
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
        return when (account.instanceType) {
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
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> misskeyAPIProvider.get(account)
                .unFollowUser(RequestUser(userId = userId.id, i = account.token))
                .throwIfHasError()
                .isSuccessful
            Account.InstanceType.MASTODON -> mastodonAPIProvider.get(account).unfollow(userId.id)
                .throwIfHasError()
                .isSuccessful
        }
    }

    suspend fun muteUser(createMute: CreateMute): UserActionResult {
        val account = accountRepository.get(createMute.userId.accountId).getOrThrow()
        return when (account.instanceType) {
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
                UserActionResult.Misskey
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
                UserActionResult.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun unmuteUser(userId: User.Id): UnMuteResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).unmuteUser(
                    RequestUser(
                        i = account.token,
                        userId = userId.id,
                    )
                ).throwIfHasError()
                UserActionResult.Misskey
            }
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account).unmuteAccount(userId.id)
                    .throwIfHasError()
                    .body()
                UserActionResult.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun blockUser(userId: User.Id): BlockUserResult {
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
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account).blockAccount(userId.id)
                    .throwIfHasError()
                    .body()
                UserActionResult.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun unblockUser(userId: User.Id): UnBlockUserResult {
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
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account).unblockAccount(userId.id)
                    .throwIfHasError()
                    .body()
                UserActionResult.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun search(
        accountId: Long,
        userName: String,
        host: String?
    ): SearchResult {
        val account = accountRepository.get(accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                val api = misskeyAPIProvider.get(account)
                val body = requireNotNull(SearchByUserAndHost(api)
                    .search(
                        RequestUser(
                            userName = userName,
                            host = host,
                            i = account.token
                        )
                    ).body()
                )
                SearchResult.Misskey(body)
            }
            Account.InstanceType.MASTODON -> {
                val body = requireNotNull(
                    mastodonAPIProvider.get(account).search(
                        if (host == null) userName else "$userName@$host"
                    ).throwIfHasError().body()
                ).accounts
                SearchResult.Mastodon(body)
            }
        }
    }
}

sealed interface UserActionResult {
    object Misskey : UserActionResult
    data class Mastodon(val relationship: MastodonAccountRelationshipDTO) : UserActionResult
}

sealed interface SearchResult {
    data class Misskey(val users: List<UserDTO>) : SearchResult
    data class Mastodon(val users: List<MastodonAccountDTO>) : SearchResult
}

typealias UnMuteResult = UserActionResult

typealias BlockUserResult = UserActionResult

typealias UnBlockUserResult = UserActionResult