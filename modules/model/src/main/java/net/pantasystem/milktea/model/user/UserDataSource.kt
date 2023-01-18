package net.pantasystem.milktea.model.user

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.AddResult


interface UserDataSource {


    /**
     * @param isSimple できるだけシンプルな構造のデータを取得する様にする。
     */
    suspend fun get(userId: User.Id, isSimple: Boolean = false): Result<User>

    /**
     * @param keepInOrder 指定したserverIdsの順番と同じように揃える場合はtrueを指定します。
     * @param isSimple できるだけシンプルな構造のデータを取得する様にする。
     */
    suspend fun getIn(
        accountId: Long,
        serverIds: List<String>,
        keepInOrder: Boolean = false,
        isSimple: Boolean = false,
    ): Result<List<User>>

    suspend fun get(accountId: Long, userName: String, host: String?): Result<User>

    suspend fun add(user: User): Result<AddResult>

    suspend fun addAll(users: List<User>): Result<List<AddResult>>

    suspend fun remove(user: User): Result<Boolean>


    fun observeIn(accountId: Long, serverIds: List<String>): Flow<List<User>>
    fun observe(userId: User.Id): Flow<User>
    fun observe(accountId: Long, acct: String): Flow<User>

    fun observe(userName: String, host: String? = null, accountId: Long): Flow<User?>

    suspend fun searchByNameOrUserName(
        accountId: Long,
        keyword: String,
        limit: Int = 100,
        nextId: String? = null,
        host: String? = null
    ): Result<List<User>>
}