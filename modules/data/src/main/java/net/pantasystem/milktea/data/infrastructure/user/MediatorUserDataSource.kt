package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.collection.LRUCache
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.db.PinnedNoteIdRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserDao
import net.pantasystem.milktea.data.infrastructure.user.db.UserEmojiRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserInfoStateRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserInstanceInfoRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserProfileFieldRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserRelatedStateRecord
import net.pantasystem.milktea.data.infrastructure.user.db.isEqualToModels
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.user.Acct
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserNotFoundException
import javax.inject.Inject

class MediatorUserDataSource @Inject constructor(
    private val userDao: UserDao,
//    private val inMem: InMemoryUserDataSource,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    loggerFactory: Logger.Factory
) : UserDataSource {

    val memCache = LRUCache<User.Id, User>(50)

    val logger = loggerFactory.create("MediatorUserDataSource")

    override suspend fun get(userId: User.Id, isSimple: Boolean): Result<User> =
        runCancellableCatching {
            withContext(ioDispatcher) {
                memCache.get(userId)
                    ?: (if (isSimple) {
                        userDao.getSimple(userId.accountId, userId.id)
                    } else {
                        userDao.get(userId.accountId, userId.id)
                    }?.toModel()?.also {
                        memCache.put(it.id, it)
                    } ?: throw UserNotFoundException(userId))
            }
        }

    override suspend fun get(accountId: Long, userName: String, host: String?): Result<User> =
        runCancellableCatching {
            withContext(ioDispatcher) {
                (if (host == null) {
                    userDao.getByUserName(accountId, userName)
                } else {
                    userDao.getByUserName(accountId, userName, host)
                })?.toModel()?.also {
                    memCache.put(it.id, it)
                } ?: throw UserNotFoundException(
                    userName = userName,
                    host = host,
                    userId = null
                )
            }
        }

    override suspend fun getIn(
        accountId: Long,
        serverIds: List<String>,
        keepInOrder: Boolean,
        isSimple: Boolean
    ): Result<List<User>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val list = serverIds.distinct().chunked(100).map { chunkedIds ->
                if (isSimple) {
                    userDao.getSimplesInServerIds(accountId, chunkedIds)
                } else {
                    userDao.getInServerIds(accountId, chunkedIds)
                }.map {
                    it.toModel()
                }
            }.flatten().also { list ->
                list.associateBy { it.id }.forEach {
                    memCache.put(it.key, it.value)
                }
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

    override suspend fun add(user: User): Result<AddResult> = runCancellableCatching {
        withContext(ioDispatcher) {
            val existsUserInMemory = memCache.get(user.id)
            if (existsUserInMemory == user) {
                return@withContext AddResult.Canceled
            }

            memCache.put(user.id, user)

            val newRecord = UserRecord.from(user)
            val record = userDao.get(user.id.accountId, user.id.id)
            val result = if (record == null) AddResult.Created else AddResult.Updated
            val dbId = if (record == null) {
                userDao.insert(newRecord)
            } else {
                userDao.update(newRecord.copy(id = record.user.id))
                record.user.id
            }

            // NOTE: 新たに追加される予定のオブジェクトと既にキャッシュしているオブジェクトの絵文字リストを比較している
            // NOTE: 比較した上で同一でなければキャッシュの更新処理を行う
            if (!record?.emojis.isEqualToModels(user.emojis)) {
                // NOTE: 既にキャッシュに存在していた場合一度全て剥がす
                if (record != null) {
                    userDao.detachAllUserEmojis(dbId)
                }
                userDao.insertEmojis(
                    user.emojis.map {
                        UserEmojiRecord.from(dbId, it)
                    }
                )
            }

            if (user is User.Detail) {
                userDao.insert(
                    UserInfoStateRecord.from(dbId, user.info)
                )
                when (val related = user.related) {
                    null -> {}
                    else -> {
                        userDao.insert(
                            UserRelatedStateRecord.from(dbId, related)
                        )
                    }
                }

                // NOTE: 更新の必要性を判定
                if ((record?.toModel() as? User.Detail?)?.info?.pinnedNoteIds?.toSet() != user.info.pinnedNoteIds?.toSet()) {
                    // NOTE: 更新系の場合は一度削除する
                    if (record != null) {
                        userDao.detachAllPinnedNoteIds(dbId)
                    }

                    if (!user.info.pinnedNoteIds.isNullOrEmpty()) {
                        userDao.insertPinnedNoteIds(user.info.pinnedNoteIds!!.map {
                            PinnedNoteIdRecord(it.noteId, userId = dbId, 0L)
                        })
                    }

                }
                if ((record?.toModel() as? User.Detail?)?.info?.fields?.toSet() != user.info.fields.toSet()) {
                    if (record != null) {
                        userDao.detachUserFields(dbId)
                    }
                    if (user.info.fields.isNotEmpty()) {
                        userDao.insertUserProfileFields(user.info.fields.map {
                            UserProfileFieldRecord(it.name, it.value, dbId)
                        })
                    }
                }
            }
            when (val instance = user.instance) {
                null -> Unit
                else -> {
                    userDao.insertUserInstanceInfo(
                        UserInstanceInfoRecord.from(dbId, instance)
                    )
                }
            }
            return@withContext result
        }
    }

    override suspend fun addAll(users: List<User>): Result<List<AddResult>> =
        runCancellableCatching {
            users.map {
                add(it).getOrElse {
                    AddResult.Canceled
                }
            }
        }

    override suspend fun remove(user: User): Result<Boolean> = runCancellableCatching {
        runCancellableCatching {
            memCache.remove(user.id)
            userDao.delete(user.id.accountId, user.id.id)
            true
        }.getOrElse {
            false
        }
    }


    override fun observeIn(accountId: Long, serverIds: List<String>): Flow<List<User>> {
        if (serverIds.isEmpty()) {
            return flowOf(emptyList())
        }
        return serverIds.distinct().chunked(500).map { chunkedIds ->
            userDao.observeInServerIds(accountId, chunkedIds).distinctUntilChanged().map { list ->
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
        }.distinctUntilChanged().flowOn(ioDispatcher).catch {
            logger.error("observeIn error", it)
            throw it
        }
    }

    override fun observe(userId: User.Id): Flow<User> {
        return userDao.observe(userId.accountId, userId.id).mapNotNull {
            it?.toModel()
        }.onEach {
            memCache.put(it.id, it)
        }.flowOn(ioDispatcher).distinctUntilChanged().catch {
            logger.error("observe by userId error", it)
            throw it
        }
    }

    override fun observe(accountId: Long, acct: String): Flow<User> {
        val (userName, host) = Acct(acct)
        return userDao.let {
            if (host == null) {
                it.observeByUserName(accountId, userName).filterNotNull()
            } else {
                it.observeByUserName(accountId, userName, host).filterNotNull()
            }
        }.map {
            it.toModel()
        }.onEach {
            memCache.put(it.id, it)
        }.flowOn(ioDispatcher).distinctUntilChanged().catch {
            logger.error("observe by acct error, acct:$acct", it)
            throw it
        }
    }

    override fun observe(userName: String, host: String?, accountId: Long): Flow<User?> {
        return if (host == null) {
            userDao.observeByUserName(userName = userName, accountId = accountId)
        } else {
            userDao.observeByUserName(userName = userName, accountId = accountId, host = host)
        }.map {
            it?.toModel()
        }.catch {
            logger.error("observe error", it)
            throw it
        }
    }


    override suspend fun searchByNameOrUserName(
        accountId: Long,
        keyword: String,
        limit: Int,
        nextId: String?,
        host: String?,
    ): Result<List<User>> = runCancellableCatching {
        withContext(ioDispatcher) {
            if (nextId == null) {
                if (host.isNullOrBlank()) {
                    userDao.searchByNameOrUserName(
                        accountId = accountId,
                        word = "$keyword%",
                        limit = limit,
                    )
                } else {
                    logger.debug("searchByNameOrUserName accountId:$accountId, keyword:$keyword, nextId:$nextId, host:$host")
                    userDao.searchByNameOrUserNameWithHost(
                        accountId = accountId,
                        word = "$keyword%",
                        limit = limit,
                        host = "$host%"
                    )
                }

            } else {
                if (host.isNullOrBlank()) {
                    userDao.searchByNameOrUserName(
                        accountId = accountId,
                        word = "$keyword%",
                        limit = limit,
                        nextId = nextId
                    )
                } else {
                    userDao.searchByNameOrUserNameWithHost(
                        accountId = accountId,
                        word = "$keyword%",
                        limit = limit,
                        nextId = nextId,
                        host = "$host%"
                    )
                }

            }.map {
                it.toModel()
            }.onEach {
                memCache.put(it.id, it)
            }

        }
    }

}