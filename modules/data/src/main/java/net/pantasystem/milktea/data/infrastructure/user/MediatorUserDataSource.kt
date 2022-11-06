package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.user.db.*
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.user.Acct
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserNotFoundException
import javax.inject.Inject

class MediatorUserDataSource @Inject constructor(
    private val userDao: UserDao,
    private val inMem: InMemoryUserDataSource,
    loggerFactory: Logger.Factory
) : UserDataSource {

    val logger = loggerFactory.create("MediatorUserDataSource")

    override suspend fun get(userId: User.Id): Result<User> = runCatching {
        withContext(Dispatchers.IO) {
            inMem.get(userId).getOrNull()
                ?: (userDao.get(userId.accountId, userId.id)?.toModel()?.also {
                    inMem.add(it).getOrThrow()
                } ?: throw UserNotFoundException(userId))
        }
    }

    override suspend fun get(accountId: Long, userName: String, host: String?): Result<User> = runCatching {
        withContext(Dispatchers.IO) {
            inMem.get(accountId, userName, host).getOrNull()
                ?: (if (host == null) {
                    userDao.getByUserName(accountId, userName)
                } else {
                    userDao.getByUserName(accountId, userName, host)
                })?.toModel()?.also {
                    inMem.add(it).getOrThrow()
                } ?: throw UserNotFoundException(userName = userName, host = host, userId = null)
        }
    }

    override suspend fun getIn(accountId: Long, serverIds: List<String>, keepInOrder: Boolean): Result<List<User>> = runCatching {
        withContext(Dispatchers.IO) {
            val list = serverIds.distinct().chunked(100).map {
                userDao.getInServerIds(accountId, serverIds).map {
                    it.toModel()
                }
            }.flatten().also {
                inMem.addAll(it).getOrThrow()
            }
            if (!keepInOrder) {
                list
            } else {
                val hash = list.associateBy { it.id.id }
                serverIds.mapNotNull {
                    hash[it]
                }
            }
        }
    }

    override suspend fun add(user: User): Result<AddResult> = runCatching {
        withContext(Dispatchers.IO) {
            val existsUserInMemory = inMem.get(user.id).getOrNull()
            if (existsUserInMemory == user) {
                return@withContext AddResult.Canceled
            }

            val result = inMem.add(user).getOrThrow()

            if (result == AddResult.Canceled) {
                return@withContext result
            }
            val newRecord = UserRecord(
                accountId = user.id.accountId,
                serverId = user.id.id,
                avatarUrl = user.avatarUrl,
                host = user.host,
                isBot = user.isBot,
                isCat = user.isCat,
                isSameHost = user.isSameHost,
                name = user.name,
                userName = user.userName,
            )
            when (result) {
                AddResult.Canceled -> {
                    return@withContext result
                }
                else -> {
                    val record = userDao.get(user.id.accountId, user.id.id)
                    val dbId = if (record == null) {
                        userDao.insert(newRecord)
                    } else {
                        userDao.update(newRecord.copy(id = record.user.id))
                        record.user.id
                    }

                    // NOTE: 新たに追加される予定のオブジェクトと既にキャッシュしているオブジェクトの絵文字リストを比較している
                    // NOTE: 比較した上で同一でなければキャッシュの更新処理を行う
                    if (record?.toModel()?.emojis?.toSet() != user.emojis.toSet()) {
                        // NOTE: 既にキャッシュに存在していた場合一度全て剥がす
                        if (record != null) {
                            userDao.detachAllUserEmojis(dbId)
                        }
                        userDao.insertEmojis(
                            user.emojis.map {
                                UserEmojiRecord(
                                    userId = dbId,
                                    name = it.name,
                                    uri = it.uri,
                                    url = it.url,
                                )
                            }
                        )
                    }

                    if (user is User.Detail) {
                        userDao.insert(
                            UserDetailedStateRecord(
                                bannerUrl = user.bannerUrl,
                                isMuting = user.isMuting,
                                isBlocking = user.isBlocking,
                                isLocked = user.isLocked,
                                isFollower = user.isFollower,
                                isFollowing = user.isFollowing,
                                description = user.description,
                                followersCount = user.followersCount,
                                followingCount = user.followingCount,
                                hasPendingFollowRequestToYou = user.hasPendingFollowRequestToYou,
                                hasPendingFollowRequestFromYou = user.hasPendingFollowRequestFromYou,
                                hostLower = user.hostLower,
                                notesCount = user.notesCount,
                                url = user.url,
                                userId = dbId,
                                birthday = user.birthday,
                                createdAt = user.createdAt,
                                updatedAt = user.updatedAt,
                            )
                        )

                        // NOTE: 更新の必要性を判定
                        if ((record?.toModel() as? User.Detail?)?.pinnedNoteIds?.toSet() != user.pinnedNoteIds?.toSet()) {
                            // NOTE: 更新系の場合は一度削除する
                            if (record != null) {
                                userDao.detachAllPinnedNoteIds(dbId)
                            }

                            if (!user.pinnedNoteIds.isNullOrEmpty()) {
                                userDao.insertPinnedNoteIds(user.pinnedNoteIds!!.map {
                                    PinnedNoteIdRecord(it.noteId, userId = dbId, 0L)
                                })
                            }

                        }
                        if ((record?.toModel() as? User.Detail?)?.fields?.toSet() != user.fields.toSet()) {
                            if (record != null) {
                                userDao.detachUserFields(dbId)
                            }
                            if (user.fields.isNotEmpty()) {
                                userDao.insertUserProfileFields(user.fields.map {
                                    UserProfileFieldRecord(it.name, it.value, dbId)
                                })
                            }
                        }
                    }
                    when (val instance = user.instance) {
                        null -> Unit
                        else -> {
                            userDao.insertUserInstanceInfo(
                                UserInstanceInfoRecord(
                                    faviconUrl = instance.faviconUrl,
                                    iconUrl = instance.iconUrl,
                                    name = instance.name,
                                    softwareVersion = instance.softwareVersion,
                                    softwareName = instance.softwareName,
                                    themeColor = instance.themeColor,
                                    userId = dbId
                                )
                            )
                        }
                    }
                }

            }
            return@withContext result
        }


    }

    override suspend fun addAll(users: List<User>): Result<List<AddResult>> = runCatching {
        users.map {
            add(it).getOrElse {
                AddResult.Canceled
            }
        }
    }

    override suspend fun remove(user: User): Result<Boolean> = runCatching {
        runCatching {
            inMem.remove(user).getOrThrow()
            userDao.delete(user.id.accountId, user.id.id)
            true
        }.getOrElse {
            false
        }
    }


    override fun observeIn(accountId: Long, serverIds: List<String>): Flow<List<User>> {
        return serverIds.distinct().chunked(50).map {
            userDao.observeInServerIds(accountId, serverIds).distinctUntilChanged().map { list ->
                list.map {
                    it.toModel()
                }
            }.distinctUntilChanged()
        }.merge().map { list ->
            val hash = list.associateBy {
                it.id.id
            }
            serverIds.mapNotNull {
                hash[it]
            }
        }.distinctUntilChanged().flowOn(Dispatchers.IO).catch {
            logger.error("observeIn error", it)
            throw it
        }
    }

    override fun observe(userId: User.Id): Flow<User> {
        return userDao.observe(userId.accountId, userId.id).mapNotNull {
            it?.toModel()
        }.onEach {
            inMem.add(it)
        }.flowOn(Dispatchers.IO).distinctUntilChanged().catch {
            logger.error("observe by userId error", it)
            throw it
        }
    }

    override fun observe(accountId: Long, acct: String): Flow<User> {
        val (userName, host) = Acct(acct).let {
            it.userName to it.host
        }
        return userDao.let {
            if(host == null) {
                it.observeByUserName(accountId, userName).filterNotNull()
            } else {
                it.observeByUserName(accountId, userName, host).filterNotNull()
            }
        }.map {
            it.toModel()
        }.onEach {
            inMem.add(it)
        }.flowOn(Dispatchers.IO).distinctUntilChanged().catch {
            logger.error("observe by acct error, acct:$acct", it)
            throw it
        }
    }

    override fun observe(userName: String, host: String?, accountId: Long): Flow<User?> {
        return inMem.observe(userName, host, accountId).distinctUntilChanged().catch {
            logger.error("observe error", it)
            throw it
        }
    }

    override suspend fun searchByName(accountId: Long, name: String): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.searchByName(accountId, "$name%").map {
                it.toModel()
            }.also {
                inMem.addAll(it)
            }
        }
    }

    override suspend fun searchByNameOrAcct(
        accountId: Long,
        keyword: String,
        limit: Int,
        nextId: String?
    ): Result<List<User>> = runCatching {
         withContext(Dispatchers.IO) {
             if (nextId == null) {
                 userDao.searchByNameOrAcct(
                     accountId = accountId,
                     word = "$keyword%",
                     limit = limit,
                 )
             } else {
                 userDao.searchByNameOrAcct(
                     accountId = accountId,
                     word = "$keyword%",
                     limit = limit,
                     nextId = nextId
                 )
             }.map {
                 it.toModel()
             }.also {
                 inMem.addAll(it)
             }

         }
    }

}