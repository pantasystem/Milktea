package jp.panta.misskeyandroidclient.model.users

interface UserRepository {

    suspend fun get(userId: String): User?

}