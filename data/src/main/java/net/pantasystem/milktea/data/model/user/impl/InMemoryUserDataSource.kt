package net.pantasystem.milktea.data.model.user.impl


import net.pantasystem.milktea.model.AddResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserNotFoundException
import net.pantasystem.milktea.model.user.UsersState
import net.pantasystem.milktea.model.user.nickname.UserNickname
import javax.inject.Inject

// TODO: 色々と依存していてよくわからないのでアーキテクチャレベルでリファクタリングをする
class InMemoryUserDataSource @Inject constructor(
    loggerFactory: Logger.Factory?,
    private val userNicknameRepository: net.pantasystem.milktea.model.user.nickname.UserNicknameRepository,
    private val accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
) : UserDataSource {
    private val logger = loggerFactory?.create("InMemoryUserDataSource")

    private var userMap = mapOf<User.Id, User>()

    private val usersLock = Mutex()

    private val _state = MutableStateFlow(UsersState())
    override val state: StateFlow<UsersState>
        get() = _state


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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun remove(user: User): Boolean {
        return usersLock.withLock {
            val map = userMap.toMutableMap()
            val result = map.remove(user.id)
            userMap = map
            result
        }?.also{
            publish(UserDataSource.Event.Removed(user.id))
        } != null


    }

    private suspend fun createOrUpdate(argUser: User): AddResult {
        // TODO: ここで変更処理までをしてしまうのは責務外なのでいつかリファクタリングをする
        val nickname = runCatching {
            val ac = accountRepository.get(argUser.id.accountId)
            userNicknameRepository.findOne(
                UserNickname.Id(argUser.userName, argUser.host?: ac.getHost())
            )
        }.getOrNull()
        val user = when(argUser) {
            is User.Detail -> argUser.copy(nickname = nickname)
            is User.Simple -> argUser.copy(nickname = nickname)
        }
        usersLock.withLock {
            val u = userMap[user.id]
            if(u == null) {
                userMap = userMap.toMutableMap().also { map ->
                    map[user.id] = user
                }
                return AddResult.CREATED
            }
            when {
                user is User.Detail -> {
                    userMap = userMap.toMutableMap().also { map ->
                        map[user.id] = user
                    }
                }
                u is User.Detail -> {
                    // RepositoryのUserがDetailで与えられたUserがSimpleの時Simpleと一致する部分のみ更新する
                    userMap = userMap.toMutableMap().also { map ->
                        map[user.id] = u.copy(
                            name = user.name,
                            userName = user.userName,
                            avatarUrl = user.avatarUrl,
                            emojis = user.emojis,
                            isCat = user.isCat,
                            isBot = user.isBot,
                            host = user.host
                        )
                    }
                }
                else -> {
                    userMap = userMap.toMutableMap().also { map ->
                        map[user.id] = user
                    }
                }
            }

            user.updated()
            return AddResult.UPDATED
        }
    }

    override suspend fun all(): List<User> {
        return userMap.values.toList()
    }

    @ExperimentalCoroutinesApi
    private fun publish(e: UserDataSource.Event) {
        _state.value = _state.value.copy(
            usersMap = userMap
        )
        logger?.debug("publish events:$e")
        listeners.forEach { listener ->
            listener.on(e)
        }
    }
}