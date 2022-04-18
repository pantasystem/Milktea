package net.pantasystem.milktea.data.model.user.impl


import net.pantasystem.milktea.model.AddResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.Logger
import javax.inject.Inject

// TODO: 色々と依存していてよくわからないのでアーキテクチャレベルでリファクタリングをする
class InMemoryUserDataSource @Inject constructor(
    loggerFactory: Logger.Factory?,
    private val userNicknameRepository: net.pantasystem.milktea.model.user.nickname.UserNicknameRepository,
    private val accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
) : net.pantasystem.milktea.model.user.UserDataSource {
    private val logger = loggerFactory?.create("InMemoryUserDataSource")

    private var userMap = mapOf<net.pantasystem.milktea.model.user.User.Id, net.pantasystem.milktea.model.user.User>()

    private val usersLock = Mutex()

    private val _state = MutableStateFlow<net.pantasystem.milktea.model.user.UsersState>(net.pantasystem.milktea.model.user.UsersState())
    override val state: StateFlow<net.pantasystem.milktea.model.user.UsersState>
        get() = _state


    private var listeners = setOf<net.pantasystem.milktea.model.user.UserDataSource.Listener>()

    override fun addEventListener(listener: net.pantasystem.milktea.model.user.UserDataSource.Listener) {
        this.listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }

    override fun removeEventListener(listener: net.pantasystem.milktea.model.user.UserDataSource.Listener) {
        this.listeners = listeners.toMutableSet().apply {
            remove(listener)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun add(user: net.pantasystem.milktea.model.user.User): AddResult {
        return createOrUpdate(user).also {
            if(it == AddResult.CREATED) {
                publish(net.pantasystem.milktea.model.user.UserDataSource.Event.Created(user.id, user))
            }else if(it == AddResult.UPDATED) {
                publish(net.pantasystem.milktea.model.user.UserDataSource.Event.Updated(user.id, user))
            }
            logger?.debug("add result:$it")
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun addAll(users: List<net.pantasystem.milktea.model.user.User>): List<AddResult> {
        return users.map {
            add(it)
        }
    }

    override suspend fun get(userId: net.pantasystem.milktea.model.user.User.Id): net.pantasystem.milktea.model.user.User {
        return usersLock.withLock {
            userMap[userId]
        }?: throw net.pantasystem.milktea.model.user.UserNotFoundException(userId)
    }

    override suspend fun get(accountId: Long, userName: String, host: String?): net.pantasystem.milktea.model.user.User {
        return usersLock.withLock {
            userMap.filterKeys {
                it.accountId == accountId
            }.map {
                it.value
            }.firstOrNull {
                it.userName == userName && (it.host == host || it.host.isNullOrBlank() == host.isNullOrBlank())
            }?: throw net.pantasystem.milktea.model.user.UserNotFoundException(null)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun remove(user: net.pantasystem.milktea.model.user.User): Boolean {
        return usersLock.withLock {
            val map = userMap.toMutableMap()
            val result = map.remove(user.id)
            userMap = map
            result
        }?.also{
            publish(net.pantasystem.milktea.model.user.UserDataSource.Event.Removed(user.id))
        } != null


    }

    private suspend fun createOrUpdate(argUser: net.pantasystem.milktea.model.user.User): AddResult {
        // TODO: ここで変更処理までをしてしまうのは責務外なのでいつかリファクタリングをする
        val nickname = runCatching {
            val ac = accountRepository.get(argUser.id.accountId)
            userNicknameRepository.findOne(
                net.pantasystem.milktea.model.user.nickname.UserNickname.Id(argUser.userName, argUser.host?: ac.getHost())
            )
        }.getOrNull()
        val user = when(argUser) {
            is net.pantasystem.milktea.model.user.User.Detail -> argUser.copy(nickname = nickname)
            is net.pantasystem.milktea.model.user.User.Simple -> argUser.copy(nickname = nickname)
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
                user is net.pantasystem.milktea.model.user.User.Detail -> {
                    userMap = userMap.toMutableMap().also { map ->
                        map[user.id] = user
                    }
                }
                u is net.pantasystem.milktea.model.user.User.Detail -> {
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

    override suspend fun all(): List<net.pantasystem.milktea.model.user.User> {
        return userMap.values.toList()
    }

    @ExperimentalCoroutinesApi
    private fun publish(e: net.pantasystem.milktea.model.user.UserDataSource.Event) {
        _state.value = _state.value.copy(
            usersMap = userMap
        )
        logger?.debug("publish events:$e")
        listeners.forEach { listener ->
            listener.on(e)
        }
    }
}