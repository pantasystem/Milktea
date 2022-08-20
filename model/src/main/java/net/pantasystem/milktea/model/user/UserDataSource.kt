package net.pantasystem.milktea.model.user

import kotlinx.coroutines.flow.Flow
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


    suspend fun get(userId: User.Id): Result<User>

    suspend fun getIn(accountId: Long, serverIds: List<String>): Result<List<User>>

    suspend fun get(accountId: Long, userName: String, host: String?): Result<User>

    suspend fun add(user: User): Result<AddResult>

    suspend fun addAll(users: List<User>): Result<List<AddResult>>

    suspend fun remove(user: User): Result<Boolean>


    fun observeIn(accountId: Long, serverIds: List<String>): Flow<List<User>>
    fun observe(userId: User.Id): Flow<User>
    fun observe(acct: String): Flow<User>

    fun observe(userName: String, host: String? = null, accountId: Long? = null): Flow<User?>

    suspend fun searchByName(accountId: Long, name: String): List<User>
}