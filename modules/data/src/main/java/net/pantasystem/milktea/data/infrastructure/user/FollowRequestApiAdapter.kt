package net.pantasystem.milktea.data.infrastructure.user

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.api.misskey.users.AcceptFollowRequest
import net.pantasystem.milktea.api.misskey.users.FollowRequestDTO
import net.pantasystem.milktea.api.misskey.users.GetFollowRequest
import net.pantasystem.milktea.api.misskey.users.RejectFollowRequest
import net.pantasystem.milktea.common.MastodonLinkHeaderDecoder
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowRequestApiAdapter @Inject constructor(
    val accountRepository: AccountRepository,
    val mastodonAPIProvider: MastodonAPIProvider,
    val misskeyAPIProvider: MisskeyAPIProvider,

) {

    suspend fun accept(userId: User.Id): FollowRequestResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account)
                    .acceptFollowRequest(
                        AcceptFollowRequest(
                            i = account.token,
                            userId = userId.id
                        )
                    ).throwIfHasError()
                FollowRequestResult.Misskey
            }
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account).acceptFollowRequest(userId.id)
                    .throwIfHasError()
                    .body()
                FollowRequestResult.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun reject(userId: User.Id): FollowRequestResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).rejectFollowRequest(
                    RejectFollowRequest(
                        i = account.token,
                        userId = userId.id
                    )
                ).throwIfHasError()
                FollowRequestResult.Misskey
            }
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account).rejectFollowRequest(userId.id)
                    .throwIfHasError()
                    .body()
                FollowRequestResult.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun findFollowRequests(accountId: Long, sinceId: String? = null, untilId: String? = null): FindFollowRequestsResult {
        val account = accountRepository.get(accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                val body = misskeyAPIProvider.get(account).getFollowRequestsList(
                    GetFollowRequest(
                        i = account.token,
                        sinceId = sinceId,
                        untilId = untilId,
                    )
                ).throwIfHasError().body()
                FindFollowRequestsResult.Misskey(
                    requireNotNull(body)
                )
            }
            Account.InstanceType.MASTODON -> {
                val res = mastodonAPIProvider.get(account).getFollowRequests(
                    maxId = untilId,
                    minId = sinceId,
                ).throwIfHasError()
                FindFollowRequestsResult.Mastodon(
                    accounts = requireNotNull(res.body()),
                    maxId = MastodonLinkHeaderDecoder(res.headers()["link"]).getMaxId(),
                    minId = MastodonLinkHeaderDecoder(res.headers()["link"]).getMinId(),
                )
            }
        }
    }
}

sealed interface FindFollowRequestsResult {
    data class Mastodon(val accounts: List<MastodonAccountDTO>, val maxId: String?, val minId: String? = null) : FindFollowRequestsResult
    data class Misskey(val userDTOs: List<FollowRequestDTO>) : FindFollowRequestsResult
}
sealed interface FollowRequestResult {
    data class Mastodon(val relationshipDTO: MastodonAccountRelationshipDTO) : FollowRequestResult
    object Misskey : FollowRequestResult
}