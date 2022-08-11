package net.pantasystem.milktea.model.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.pantasystem.milktea.model.AddResult

data class UsersState (
    val usersMap: Map<User.Id, User> = emptyMap()
) {

    fun get(userId: User.Id?): User? {
        return usersMap[userId]
    }

    fun get(userName: String, host: String? = null, accountId: Long? = null): User? {
        return usersMap.values.filter {
            accountId == null || accountId == it.id.accountId
        }.firstOrNull {
            it.userName == userName && it.host == host
        }
    }

    fun get(fqdnUserName: String): User? {
        val userNameAndHost = fqdnUserName.split("@").filter { it.isNotBlank() }
        val userName = userNameAndHost[0]
        val host = userNameAndHost.getOrNull(1)
        return get(userName, host)
    }
}

interface UserDataSource {


    sealed class Event{
        abstract val userId: User.Id
        data class Updated(override val userId: User.Id, val user: User): Event()
        data class Removed(override val userId: User.Id): Event()
        data class Created(override val userId: User.Id, val user: User): Event()
    }

    fun interface Listener {
        fun on(e: Event)
    }

    fun addEventListener(listener: Listener)

    fun removeEventListener(listener: Listener)

    val state: StateFlow<UsersState>

    suspend fun get(userId: User.Id): User

    suspend fun getIn(userIds: List<User.Id>): List<User>

    suspend fun get(accountId: Long, userName: String, host: String?): User

    suspend fun add(user: User): AddResult

    suspend fun addAll(users: List<User>): List<AddResult>

    suspend fun remove(user: User): Boolean

    suspend fun all(): List<User>

    fun observeIn(userIds: List<User.Id>): Flow<List<User>>
    fun observe(userId: User.Id): Flow<User>
    fun observe(acct: String): Flow<User>

    fun observe(userName: String, host: String? = null, accountId: Long? = null): Flow<User?>
}