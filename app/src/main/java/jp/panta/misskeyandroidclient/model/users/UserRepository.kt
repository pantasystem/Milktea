package jp.panta.misskeyandroidclient.model.users

interface UserRepository {

    suspend fun find(userId: User.Id, detail: Boolean = true): User

    suspend fun findByUserName(accountId: Long, userName: String, host: String?, detail: Boolean = true): User

    suspend fun follow(userId: User.Id): Boolean

    suspend fun unfollow(userId: User.Id): Boolean

    suspend fun mute(userId: User.Id): Boolean

    suspend fun unmute(userId: User.Id): Boolean

    suspend fun block(userId: User.Id): Boolean

    suspend fun unblock(userId: User.Id): Boolean

    suspend fun acceptFollowRequest(userId: User.Id) : Boolean

}