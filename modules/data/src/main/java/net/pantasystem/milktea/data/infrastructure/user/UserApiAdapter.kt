package net.pantasystem.milktea.data.infrastructure.user

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
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
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

internal interface UserApiAdapter {

    suspend fun show(userId: User.Id, detail: Boolean): User

    suspend fun search(
        accountId: Long,
        userName: String,
        host: String?,
    ): SearchResult

}

@Singleton
internal class UserApiAdapterImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    private val nodeInfoRepository: NodeInfoRepository,
) : UserApiAdapter {

    override suspend fun show(userId: User.Id, detail: Boolean): User {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
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
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
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



    override suspend fun search(
        accountId: Long,
        userName: String,
        host: String?,
    ): SearchResult {
        val account = accountRepository.get(accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                val api = misskeyAPIProvider.get(account)
                val body = requireNotNull(
                    SearchByUserAndHost(api, nodeInfoRepository, account)
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
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val body = requireNotNull(
                    mastodonAPIProvider.get(account).search(
                        if (host == null) userName else "$userName@$host",
                        type = "accounts"
                    ).throwIfHasError().body()
                ).accounts
                SearchResult.Mastodon(body)
            }
        }
    }
}


sealed interface SearchResult {
    data class Misskey(val users: List<UserDTO>) : SearchResult
    data class Mastodon(val users: List<MastodonAccountDTO>) : SearchResult
}

