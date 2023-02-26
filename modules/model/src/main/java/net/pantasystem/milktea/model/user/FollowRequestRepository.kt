package net.pantasystem.milktea.model.user

interface FollowRequestRepository {

    suspend fun accept(userId: User.Id) : Boolean

    suspend fun reject(userId: User.Id) : Boolean

    suspend fun find(
        accountId: Long,
        sinceId: String? = null,
        untilId: String? = null,
    ): FollowRequestsResult
}

data class FollowRequestsResult(
    val users: List<User.Detail>,
    val sinceId: String? = null,
    val untilId: String? = null,
)