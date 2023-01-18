package net.pantasystem.milktea.data.infrastructure.user


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.users.*
import net.pantasystem.milktea.api.misskey.users.report.ReportDTO
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserNotFoundException
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.mute.CreateMute
import net.pantasystem.milktea.model.user.query.FindUsersQuery
import net.pantasystem.milktea.model.user.report.Report
import retrofit2.Response
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
class UserRepositoryImpl @Inject constructor(
    val userDataSource: UserDataSource,
    val noteDataSource: NoteDataSource,
    val filePropertyDataSource: FilePropertyDataSource,
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val loggerFactory: Logger.Factory,
    @IODispatcher val ioDispatcher: CoroutineDispatcher,
) : UserRepository {
    private val logger: Logger by lazy {
        loggerFactory.create("UserRepositoryImpl")
    }
    private val noteDataSourceAdder =
        NoteDataSourceAdder(userDataSource, noteDataSource, filePropertyDataSource)

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

            val account = accountRepository.get(userId.accountId).getOrThrow()
            if (localResult.getOrNull() == null) {
                val res = misskeyAPIProvider.get(account).showUser(
                    RequestUser(
                        i = account.token,
                        userId = userId.id,
                        detail = true
                    )
                )
                res.throwIfHasError()
                res.body()?.let {
                    val user = it.toUser(account, true)
                    it.pinnedNotes?.forEach { dto ->
                        noteDataSourceAdder.addNoteDtoToDataSource(account, dto)
                    }
                    val result = userDataSource.add(user)
                    logger.debug("add result: $result")
                    return@withContext userDataSource.get(userId).getOrThrow()
                }
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
            it.pinnedNotes?.forEach { dto ->
                noteDataSourceAdder.addNoteDtoToDataSource(account, dto)
            }
            val user = it.toUser(account, detail)
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
            val i = ac.token
            val api = misskeyAPIProvider.get(ac)

            val results = SearchByUserAndHost(api)
                .search(
                    RequestUser(
                        userName = userName,
                        host = host,
                        i = i
                    )
                )
                .throwIfHasError()

            results.body()!!.forEach {
                it.toUser(ac, true).also { u ->
                    userDataSource.add(u)
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

    override suspend fun mute(createMute: CreateMute): Boolean = withContext(ioDispatcher) {
        val account = accountRepository.get(createMute.userId.accountId).getOrThrow()
        val res = misskeyAPIProvider.get(account).muteUser(
            CreateMuteUserRequest(
                i = account.token,
                userId = createMute.userId.id,
                expiresAt = createMute.expiresAt?.toEpochMilliseconds()
            )
        ).throwIfHasError()
        userDataSource.add(userDataSource.get(createMute.userId).getOrThrow()).getOrThrow()
        res.isSuccessful
    }

    override suspend fun unmute(userId: User.Id): Boolean = withContext(ioDispatcher) {
        action(userId.getMisskeyAPI()::unmuteUser, userId) { user ->
            user.copy(
                related = user.related?.copy(
                    isMuting = false
                )
            )
        }
    }

    override suspend fun block(userId: User.Id): Boolean = withContext(ioDispatcher) {
        action(userId.getMisskeyAPI()::blockUser, userId) { user ->
            user.copy(
                related = user.related?.copy(
                    isBlocking = true
                )
            )
        }
    }

    override suspend fun unblock(userId: User.Id): Boolean = withContext(ioDispatcher) {
        action(userId.getMisskeyAPI()::unblockUser, userId) { user ->
            user.copy(
                related = user.related?.copy(isBlocking = false)
            )
        }
    }

    override suspend fun follow(userId: User.Id): Boolean = withContext(ioDispatcher) {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        val user = find(userId, true) as User.Detail
        val req = RequestUser(userId = userId.id, i = account.token)
        logger.debug("follow req:$req")
        val res = misskeyAPIProvider.get(account).followUser(req)
        res.throwIfHasError()
        if (res.isSuccessful) {
            val updated = (find(userId, true) as User.Detail).copy(
                related = (if (user.info.isLocked) user.related?.isFollowing else true)?.let {
                    (if (user.info.isLocked) true else user.related?.hasPendingFollowRequestFromYou)?.let { it1 ->
                        user.related?.copy(
                            isFollowing = it,
                            hasPendingFollowRequestFromYou = it1
                        )
                    }
                }
            )
            userDataSource.add(updated)
        }
        res.isSuccessful
    }

    override suspend fun unfollow(userId: User.Id): Boolean = withContext(ioDispatcher) {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        val user = find(userId, true) as User.Detail


        val res = if (user.info.isLocked) {
            misskeyAPIProvider.get(account)
                .cancelFollowRequest(CancelFollow(userId = userId.id, i = account.token))
        } else {
            misskeyAPIProvider.get(account)
                .unFollowUser(RequestUser(userId = userId.id, i = account.token))
        }
        res.throwIfHasError()
        if (res.isSuccessful) {
            val updated = user.copy(
                related = user.related?.let{
                    it.copy(
                        isFollowing = if (user.info.isLocked) it.isFollowing else false,
                        hasPendingFollowRequestFromYou = if (user.info.isLocked) false else it.hasPendingFollowRequestFromYou
                    )
                }
            )
            userDataSource.add(updated)
        }
        res.isSuccessful
    }

    override suspend fun acceptFollowRequest(userId: User.Id): Boolean =
        withContext(ioDispatcher) {
            val account = accountRepository.get(userId.accountId).getOrThrow()
            val user = find(userId, true) as User.Detail
            if (user.related?.hasPendingFollowRequestToYou != true) {
                return@withContext false
            }
            val res = misskeyAPIProvider.get(account)
                .acceptFollowRequest(
                    AcceptFollowRequest(
                        i = account.token,
                        userId = userId.id
                    )
                )
                .throwIfHasError()
            if (res.isSuccessful) {
                userDataSource.add(
                    user.copy(
                        related = user.related?.copy(
                            hasPendingFollowRequestToYou = false,
                            isFollower = true
                        )
                    )
                )
            }
            return@withContext res.isSuccessful

        }

    override suspend fun rejectFollowRequest(userId: User.Id): Boolean =
        withContext(ioDispatcher) {
            val account = accountRepository.get(userId.accountId).getOrThrow()
            val user = find(userId, true) as User.Detail
            if (user.related?.hasPendingFollowRequestToYou != true) {
                return@withContext false
            }
            val res = misskeyAPIProvider.get(account).rejectFollowRequest(
                RejectFollowRequest(
                    i = account.token,
                    userId = userId.id
                )
            )
                .throwIfHasError()
            if (res.isSuccessful) {
                userDataSource.add(
                    user.copy(
                        related = user.related?.copy(
                            hasPendingFollowRequestToYou = false,
                            isFollower = false
                        )
                    )
                )
            }
            return@withContext res.isSuccessful
        }

    private suspend fun action(
        requestAPI: suspend (RequestUser) -> Response<Unit>,
        userId: User.Id,
        reducer: (User.Detail) -> User.Detail
    ): Boolean {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(userId.accountId).getOrThrow()
            val res = requestAPI.invoke(RequestUser(userId = userId.id, i = account.token))
            res.throwIfHasError()
            if (res.isSuccessful) {

                val updated = reducer.invoke(find(userId, true) as User.Detail)
                userDataSource.add(updated)
            }
            return@withContext res.isSuccessful
        }
    }

    override suspend fun report(report: Report): Boolean {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(report.userId.accountId).getOrThrow()
            val api = report.userId.getMisskeyAPI()
            val res = api.report(
                ReportDTO(
                    i = account.token,
                    comment = report.comment,
                    userId = report.userId.id
                )
            )
            res.throwIfHasError()
            res.isSuccessful
        }
    }

    override suspend fun findUsers(accountId: Long, query: FindUsersQuery): List<User> {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            val request = RequestUser.from(query, account.token)
            val res = misskeyAPIProvider.get(account).getUsers(request)
                .throwIfHasError()
            res.body()?.map {
                it.toUser(account, true)
            }?.onEach {
                userDataSource.add(it)
            } ?: emptyList()
        }
    }

    override suspend fun sync(userId: User.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val account = accountRepository.get(userId.accountId)
                    .getOrThrow()
                val user = misskeyAPIProvider.get(account)
                    .showUser(
                        RequestUser(i = account.token, userId = userId.id, detail = true)
                    ).throwIfHasError()
                    .body()!!.toUser(account, true)
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
                            it.toUser(account, true)
                        }
                    userDataSource.addAll(users)
                    users.map { it.id }
                }
            }
        }
    }

    private suspend fun User.Id.getMisskeyAPI(): MisskeyAPI {
        return misskeyAPIProvider.get(accountRepository.get(accountId).getOrThrow())
    }
}