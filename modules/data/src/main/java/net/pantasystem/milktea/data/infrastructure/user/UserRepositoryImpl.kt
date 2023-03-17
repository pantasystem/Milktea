package net.pantasystem.milktea.data.infrastructure.user


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.users.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserNotFoundException
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.query.FindUsersQuery
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
internal class UserRepositoryImpl @Inject constructor(
    val userDataSource: UserDataSource,
    val filePropertyDataSource: FilePropertyDataSource,
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val loggerFactory: Logger.Factory,
    val userApiAdapter: UserApiAdapter,
    val userDTOEntityConverter: UserDTOEntityConverter,
    @IODispatcher val ioDispatcher: CoroutineDispatcher,
) : UserRepository {
    private val logger: Logger by lazy {
        loggerFactory.create("UserRepositoryImpl")
    }

    override suspend fun find(userId: User.Id, detail: Boolean): User =
        withContext(ioDispatcher) {
            val localResult = runCancellableCatching {
                userDataSource.get(userId).let {
                    if (detail) {
                        it.getOrThrow() as? User.Detail
                    } else it.getOrThrow()
                }
            }.onFailure {
                logger.debug("ローカルにユーザーは存在しませんでした。:$userId")
            }
            localResult.getOrNull()?.let {
                return@withContext it
            }

            if (localResult.getOrNull() == null) {
                val user = userApiAdapter.show(userId, detail)
                val result = userDataSource.add(user)
                logger.debug("add result: $result")
                return@withContext userDataSource.get(userId).getOrThrow()
            }

            throw UserNotFoundException(userId)
        }

    override suspend fun findByUserName(
        accountId: Long,
        userName: String,
        host: String?,
        detail: Boolean
    ): User = withContext(ioDispatcher) {
        val local = runCancellableCatching {
            userDataSource.get(accountId, userName, host).let {
                if (detail) {
                    it.getOrThrow() as? User.Detail
                } else it.getOrThrow()
            }
        }.getOrNull()

        logger.debug("local:$local")
        if (local != null) {
            return@withContext local
        }
        val account = accountRepository.get(accountId).getOrThrow()
        val misskeyAPI = misskeyAPIProvider.get(account.normalizedInstanceDomain)
        val res = misskeyAPI.showUser(
            RequestUser(
                i = account.token,
                userName = userName,
                host = host,
                detail = detail
            )
        )
        logger.debug("res:$res")
        res.throwIfHasError()

        res.body()?.let {
            val user = userDTOEntityConverter.convert(account, it, detail)
            userDataSource.add(user)
            return@withContext userDataSource.get(user.id).getOrThrow()
        }

        throw UserNotFoundException(
            null,
            userName = userName,
            host = host
        )

    }


    override suspend fun syncByUserName(
        accountId: Long,
        userName: String,
        host: String?
    ) = runCancellableCatching<Unit> {
        withContext(ioDispatcher) {
            val ac = accountRepository.get(accountId).getOrThrow()

            when(val result = userApiAdapter.search(accountId, userName, host)) {
                is SearchResult.Mastodon -> {
                    userDataSource.addAll(
                        result.users.map {
                            it.toModel(ac)
                        }
                    )
                }
                is SearchResult.Misskey -> {
                    result.users.forEach {
                        userDTOEntityConverter.convert(ac, it, true).also { u ->
                            userDataSource.add(u)
                        }
                    }
                }
            }
        }
    }

    override suspend fun searchByNameOrUserName(
        accountId: Long,
        keyword: String,
        limit: Int,
        nextId: String?,
        host: String?
    ): List<User> {
        return userDataSource.searchByNameOrUserName(accountId, keyword, limit, nextId, host = host)
            .getOrThrow()
    }


    override suspend fun findUsers(accountId: Long, query: FindUsersQuery): List<User> {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            val request = RequestUser.from(query, account.token)
            val res = misskeyAPIProvider.get(account).getUsers(request)
                .throwIfHasError()
            res.body()?.map {
                userDTOEntityConverter.convert(account, it, true)
            }?.onEach {
                userDataSource.add(it)
            } ?: emptyList()
        }
    }

    override suspend fun sync(userId: User.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val user = userApiAdapter.show(userId, true)
                userDataSource.add(user)
            }
        }
    }

    override suspend fun syncIn(userIds: List<User.Id>): Result<List<User.Id>> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val accountId = userIds.map { it.accountId }.distinct().firstOrNull()
                if (accountId == null) {
                    emptyList()
                } else {
                    val account = accountRepository.get(accountId)
                        .getOrThrow()
                    val users = misskeyAPIProvider.get(account)
                        .showUsers(
                            RequestUser(
                                i = account.token,
                                userIds = userIds.map { it.id },
                                detail = true
                            )
                        ).throwIfHasError()
                        .body()!!.map {
                            userDTOEntityConverter.convert(account, it, true)
                        }
                    userDataSource.addAll(users)
                    users.map { it.id }
                }
            }
        }
    }

}