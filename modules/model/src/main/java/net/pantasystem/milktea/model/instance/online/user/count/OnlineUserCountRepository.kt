package net.pantasystem.milktea.model.instance.online.user.count

interface OnlineUserCountRepository {
    suspend fun find(accountId: Long): Result<OnlineUserCountResult>
}

sealed interface OnlineUserCountResult {
    object Unknown : OnlineUserCountResult
    data class Success(val count: Int) : OnlineUserCountResult
}