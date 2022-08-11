package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.user.db.UserDetailedStateRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserEmojiRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserRecord
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserNotFoundException
import javax.inject.Inject

class MediatorUserDataSource @Inject constructor(
    private val dataBase: DataBase,
    private val inMem: InMemoryUserDataSource,

) : UserDataSource {
    override fun addEventListener(listener: UserDataSource.Listener) {
        inMem.addEventListener(listener)
    }

    override fun removeEventListener(listener: UserDataSource.Listener) {
        inMem.removeEventListener(listener)
    }

    override suspend fun get(userId: User.Id): User {
        return runCatching {
            inMem.get(userId)
        }.getOrNull()
            ?: (dataBase.userDao().get(userId.accountId, userId.id)?.toModel()?.also {
                inMem.add(it)
            } ?: throw UserNotFoundException(userId))

    }

    override suspend fun get(accountId: Long, userName: String, host: String?): User {
        return runCatching {
            inMem.get(accountId, userName, host)
        }.getOrNull()
            ?: (if (host == null) {
                dataBase.userDao().getByUserName(accountId, userName)
            } else {
                dataBase.userDao().getByUserName(accountId, userName, host)
            })?.toModel()?.also {
                inMem.add(it)
            } ?: throw UserNotFoundException(userName = userName, host = host, userId = null)
    }

    override suspend fun getIn(userIds: List<User.Id>): List<User> {

        val accAndId = userIds.groupBy {
            it.accountId
        }
        val users = accAndId.map { group ->
            dataBase.userDao().getInServerIds(group.key, group.value.map { it.id })
        }.map { list ->
            list.map {
                it.toModel()
            }
        }.flatten()
        inMem.addAll(users)
        return users
    }

    override suspend fun add(user: User): AddResult {
        return withContext(Dispatchers.IO) {
            val result = inMem.add(user)

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
                    val record = dataBase.userDao().get(user.id.accountId, user.id.id)
                    val dbId = if (record == null) {
                        dataBase.userDao().insert(newRecord)
                    } else {
                        dataBase.userDao().update(newRecord.copy(id = record.user.id))
                        record.user.id
                    }
                    dataBase.userDao().insertEmojis(
                        user.emojis.map {
                            UserEmojiRecord(
                                userId = dbId,
                                name = it.name,
                                uri = it.uri,
                                url = it.url,
                            )
                        }
                    )
                    if (user is User.Detail) {
                        dataBase.userDao().insert(
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
                                userId = dbId
                            )
                        )
                    }
                }

            }
            return@withContext result
        }


    }

    override suspend fun addAll(users: List<User>): List<AddResult> {
        return users.map {
            add(it)
        }
    }

    override suspend fun remove(user: User): Boolean {
        return inMem.remove(user)
    }

    override suspend fun all(): List<User> {
        return inMem.all()
    }

    override fun observeIn(userIds: List<User.Id>): Flow<List<User>> {
        return inMem.observeIn(userIds).distinctUntilChanged()
    }

    override fun observe(userId: User.Id): Flow<User> {
        return dataBase.userDao().observe(userId.accountId, userId.id).mapNotNull {
            it?.toModel()
        }.onEach {
            inMem.add(it)
        }.flowOn(Dispatchers.IO).distinctUntilChanged()
    }

    override fun observe(acct: String): Flow<User> {
        val userNameAndHost = acct.split("@").filter { it.isNotBlank() }
        val userName = userNameAndHost[0]
        val host = userNameAndHost.getOrNull(1)
        return dataBase.userDao().let {
            if(host == null) {
                it.observeByUserName(userName).filterNotNull()
            } else {
                it.observeByUserName(userName, host).filterNotNull()
            }
        }.map {
            it.toModel()
        }.onEach {
            inMem.add(it)
        }.flowOn(Dispatchers.IO).distinctUntilChanged()
    }

    override fun observe(userName: String, host: String?, accountId: Long?): Flow<User?> {
        return inMem.observe(userName, host, accountId).distinctUntilChanged()
    }


}