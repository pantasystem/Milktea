package net.pantasystem.milktea.data.infrastructure.user


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserNotFoundException
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.query.FindUsersQuery
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource,
    private val loggerFactory: Logger.Factory,
    private val userApiAdapter: UserApiAdapter,
    private val userSuggestionsApiAdapter: UserSuggestionsApiAdapter,
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
                val result = userDataSource.add(user).getOrThrow()
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
        val user = userApiAdapter.showByUserName(accountId, userName, host, detail).also {
            userDataSource.add(it)

        }
        userDataSource.get(user.id).getOrThrow()
    }


    override suspend fun syncByUserName(
        accountId: Long,
        userName: String,
        host: String?
    ) = runCancellableCatching<Unit> {
        withContext(ioDispatcher) {
            val users = userApiAdapter.search(accountId, userName, host)
            userDataSource.addAll(users).getOrThrow()
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
            val result = userSuggestionsApiAdapter.showSuggestions(accountId, query)
            userDataSource.addAll(result).getOrThrow()
            result
        }
    }

    override suspend fun sync(userId: User.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val user = userApiAdapter.show(userId, true)
                userDataSource.add(user).getOrThrow()
            }
        }
    }

    override suspend fun syncIn(userIds: List<User.Id>): Result<List<User.Id>> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val users = userApiAdapter.showUsers(userIds, true)
                userDataSource.addAll(users).getOrThrow()
                users.map {
                    it.id
                }
            }
        }
    }

    override fun observe(userId: User.Id): Flow<User> {
        return userDataSource.observe(userId)
    }

    override fun observe(accountId: Long, acct: String): Flow<User> {
        return userDataSource.observe(accountId, acct)
    }

    override fun observe(userName: String, host: String?, accountId: Long): Flow<User?> {
        return userDataSource.observe(userName, host, accountId)
    }

    override fun observeIn(accountId: Long, serverIds: List<String>): Flow<List<User>> {
        return userDataSource.observeIn(accountId, serverIds)
    }

}