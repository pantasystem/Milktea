package jp.panta.misskeyandroidclient.model.users.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserNotFoundException
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserDataSource(
    loggerFactory: Logger.Factory? = null
) : UserDataSource{
    private val logger = loggerFactory?.create("InMemoryUserDataSource")

    private val userMap = ConcurrentHashMap<User.Id, User>()

    private val usersLock = Mutex()


    private var listeners = setOf<UserDataSource.Listener>()

    override fun addEventListener(listener: UserDataSource.Listener) {
        this.listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }

    override fun removeEventListener(listener: UserDataSource.Listener) {
        this.listeners = listeners.toMutableSet().apply {
            remove(listener)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun add(user: User): AddResult {
        return createOrUpdate(user).also {
            if(it == AddResult.CREATED) {
                publish(UserDataSource.Event.Created(user.id, user))
            }else if(it == AddResult.UPDATED) {
                publish(UserDataSource.Event.Updated(user.id, user))
            }
            logger?.debug("add result:$it")
        }

    }

    @ExperimentalCoroutinesApi
    override suspend fun addAll(users: List<User>): List<AddResult> {
        return users.map {
            add(it)
        }
    }

    override suspend fun get(userId: User.Id): User {
        return usersLock.withLock {
            userMap[userId]
        }?: throw UserNotFoundException(userId)
    }

    override suspend fun get(accountId: Long, userName: String, host: String?): User {
        return usersLock.withLock {
            userMap.filterKeys {
                it.accountId == accountId
            }.map {
                it.value
            }.firstOrNull {
                it.userName == userName && (it.host == host || it.host.isNullOrBlank() == host.isNullOrBlank())
            }?: throw UserNotFoundException(null)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun remove(user: User): Boolean {
        return usersLock.withLock {
            userMap.remove(user.id)
        }?.also{
            publish(UserDataSource.Event.Removed(user.id))
        } != null


    }

    private suspend fun createOrUpdate(user: User): AddResult {
        usersLock.withLock {
            val u = userMap[user.id]
            if(u == null) {
                userMap[user.id] = user
                return AddResult.CREATED
            }
            if(u.instanceUpdatedAt > user.instanceUpdatedAt){
                return AddResult.CANCEL
            }
            when {
                user is User.Detail -> {
                    userMap[user.id] = user
                }
                u is User.Detail -> {
                    // RepositoryのUserがDetailで与えられたUserがSimpleの時Simpleと一致する部分のみ更新する
                    userMap[user.id] = u.copy(
                        name = user.name,
                        userName = user.userName,
                        avatarUrl = user.avatarUrl,
                        emojis = user.emojis,
                        isCat = user.isCat,
                        isBot = user.isBot,
                        host = user.host
                    )
                }
                else -> {
                    userMap[user.id] = user
                }
            }

            user.updated()
            return AddResult.UPDATED
        }
    }


    @ExperimentalCoroutinesApi
    private fun publish(e: UserDataSource.Event) {
        listeners.forEach { listener ->
            listener.on(e)
        }
    }
}