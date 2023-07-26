package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.api.misskey.users.SearchByUserAndHost
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.users.from
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history.ReactionHistoryDao
import net.pantasystem.milktea.data.infrastructure.toUserRelated
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserNotFoundException
import net.pantasystem.milktea.model.user.query.FindUsersFromFrequentlyReactionUsers
import net.pantasystem.milktea.model.user.query.FindUsersQuery
import net.pantasystem.milktea.model.user.query.FindUsersQuery4Mastodon
import net.pantasystem.milktea.model.user.query.FindUsersQuery4Misskey
import javax.inject.Inject
import javax.inject.Singleton

internal interface UserApiAdapter {

    suspend fun show(userId: User.Id, detail: Boolean): User

    suspend fun search(
        accountId: Long,
        userName: String,
        host: String?,
    ): List<User>

    suspend fun showUsers(
        userIds: List<User.Id>,
        detail: Boolean,
    ): List<User>


    suspend fun showByUserName(
        accountId: Long,
        userName: String,
        host: String?,
        detail: Boolean,
    ): User

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
    ): List<User> {
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
                body.map {
                    userDTOEntityConverter.convert(account, it, true)
                }
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                requireNotNull(
                    mastodonAPIProvider.get(account).search(
                        if (host == null) userName else "$userName@$host",
                        type = "accounts"
                    ).throwIfHasError().body()
                ).accounts.map {
                    it.toModel(account)
                }

            }
        }
    }

    override suspend fun showUsers(userIds: List<User.Id>, detail: Boolean): List<User> {
        val accountIds = userIds.map { it.accountId }.distinct()
        return coroutineScope {
            accountIds.map { accountId ->
                async {
                    val account = accountRepository.get(accountId).getOrThrow()
                    when(account.instanceType) {
                        Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                            misskeyAPIProvider.get(account)
                                .showUsers(
                                    RequestUser(
                                        i = account.token,
                                        userIds = userIds.filter { it.accountId == accountId }.map { it.id },
                                        detail = true
                                    )
                                ).throwIfHasError()
                                .body()!!.map {
                                    userDTOEntityConverter.convert(account, it, true)
                                }


                        }
                        Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                            userIds.filter { it.accountId == accountId }.map { it.id }.map {
                                async {
                                    requireNotNull(
                                        mastodonAPIProvider.get(account)
                                            .getAccount(it)
                                            .throwIfHasError()
                                            .body()
                                    ).toModel(account)
                                }
                            }.awaitAll()

                        }
                    }

                }
            }.awaitAll()
        }.flatten()
    }

    override suspend fun showByUserName(
        accountId: Long,
        userName: String,
        host: String?,
        detail: Boolean
    ): User {
        val account = accountRepository.get(accountId).getOrThrow()
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                val misskeyAPI = misskeyAPIProvider.get(account.normalizedInstanceUri)
                val res = misskeyAPI.showUser(
                    RequestUser(
                        i = account.token,
                        userName = userName,
                        host = host,
                        detail = detail
                    )
                )
                res.throwIfHasError()
                res.body()?.let {
                    userDTOEntityConverter.convert(account, it, detail)
                }
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val result = mastodonAPIProvider.get(account).search(
                    if (host == null) "@$userName" else "@$userName@$host",
                    resolve = true,
                ).throwIfHasError().body()
                val dto = requireNotNull(result).accounts.firstOrNull {
                    it.username == userName && (host == null || it.acct.endsWith("@$host"))
                }
                val relationships = if (detail) {
                    mastodonAPIProvider.get(account).getAccountRelationships(
                        listOf(dto!!.id)
                    ).throwIfHasError().body()?.associate {
                        it.id to it
                    }
                } else {
                    emptyMap()
                }
                dto?.toModel(account, relationships?.get(dto.id)?.toUserRelated())
            }
        } ?: throw UserNotFoundException(
            null,
            userName = userName,
            host = host
        )

    }
}


internal interface UserSuggestionsApiAdapter {

    suspend fun showSuggestions(accountId: Long, query: FindUsersQuery): List<User>

}


class UserSuggestionsApiAdapterImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val reactionHistoryDao: ReactionHistoryDao,
    private val userDataSource: UserDataSource,
    private val userDTOEntityConverter: UserDTOEntityConverter,
): UserSuggestionsApiAdapter {
    override suspend fun showSuggestions(accountId: Long, query: FindUsersQuery): List<User> {
        val account = accountRepository.get(accountId).getOrThrow()
        return when(query) {
            is FindUsersQuery4Mastodon.SuggestUsers -> {
                val api = mastodonAPIProvider.get(account)
                val body = requireNotNull(api.getSuggestionUsers(
                    limit = query.limit
                ).throwIfHasError().body())
                val accounts = body.map {
                    it.account
                }
                val relationships = requireNotNull(
                    api.getAccountRelationships(ids = accounts.map { it.id })
                        .throwIfHasError()
                        .body()
                ).let { list ->
                    list.associateBy {
                        it.id
                    }
                }
                val models = accounts.map {
                    it.toModel(account, relationships[it.id]?.toUserRelated())
                }
                models
            }
            is FindUsersQuery4Misskey -> {
                val request = RequestUser.from(query, account.token)
                val res = misskeyAPIProvider.get(account).getUsers(request)
                    .throwIfHasError()
                res.body()?.map {
                    userDTOEntityConverter.convert(account, it, true)
                }?.onEach {
                    userDataSource.add(it)
                } ?: emptyList()
            }
            is FindUsersFromFrequentlyReactionUsers -> {
                val userIds = reactionHistoryDao.findFrequentlyReactionUserAndUnFollowed(
                    accountId = accountId,
                    limit = 20,
                ).map {
                    it.targetUserId
                }
                userDataSource.getIn(accountId, userIds).getOrThrow()
            }
        }
    }
}


sealed interface SearchResult {
    data class Misskey(val users: List<UserDTO>) : SearchResult
    data class Mastodon(val users: List<MastodonAccountDTO>) : SearchResult
}

