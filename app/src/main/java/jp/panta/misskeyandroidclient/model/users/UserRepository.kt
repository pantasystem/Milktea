package jp.panta.misskeyandroidclient.model.users

interface UserRepository {

    suspend fun get(userId: String): User?

    suspend fun add(user: User): Boolean

    suspend fun remove(user: User): Boolean
}