package jp.panta.misskeyandroidclient.model.users.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserRepository : UserRepository{

    private val userMap = ConcurrentHashMap<User.Id, User>()
    private val recordLocks = ConcurrentHashMap<User.Id, Mutex>()
    private val tableLock = Mutex()

    @ExperimentalCoroutinesApi
    private val broadcast = BroadcastChannel<UserRepository.Event>(1)

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun observable(): Flow<UserRepository.Event> {
        return broadcast.asFlow()
    }

    private val listeners = mutableSetOf<UserRepository.Listener>()

    override fun addEventListener(listener: UserRepository.Listener) {
        this.listeners.add(listener)
    }

    override fun removeEventListener(listener: UserRepository.Listener) {
        this.listeners.remove(listener)
    }

    @ExperimentalCoroutinesApi
    override suspend fun add(user: User): AddResult {
        recordLocks[user.id]?.withLock {
            val u = userMap[user.id]?: throw IllegalStateException("recordLocksにはオブジェクトがあるのにuserMap二は存在しない異常な状態です。")
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
            publish(UserRepository.Event.Updated(user.id, user))
            return AddResult.UPDATED

        }?: tableLock.withLock {
            userMap[user.id] = user
            recordLocks[user.id] = Mutex()
            publish(UserRepository.Event.Created(user.id, user))
            return AddResult.CREATED
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun addAll(users: List<User>): List<AddResult> {
        return users.map {
            add(it)
        }
    }

    override suspend fun get(userId: User.Id): User? {
        return recordLocks[userId]?.withLock {
            userMap[userId]
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun remove(user: User): Boolean {
        return tableLock.withLock {
            recordLocks[user.id]?.withLock {
                userMap.remove(user.id)
                publish(UserRepository.Event.Removed(user.id))
                true
            }?: false
        }

    }

    @ExperimentalCoroutinesApi
    private fun publish(e: UserRepository.Event) {
        synchronized(listeners) {
            listeners.forEach { listener ->
                listener.on(e)
            }
        }

        broadcast.offer(e)
    }
}