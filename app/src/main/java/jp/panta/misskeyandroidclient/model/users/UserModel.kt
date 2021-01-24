package jp.panta.misskeyandroidclient.model.users

interface UserModel {

    suspend fun get(userId: String): User?

    suspend fun follow(userId: String)

    suspend fun unfollow(userId: String)

    suspend fun block(user: User)

    suspend fun unblock(user: User)

}