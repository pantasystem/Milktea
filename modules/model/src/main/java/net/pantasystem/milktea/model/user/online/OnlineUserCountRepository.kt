package net.pantasystem.milktea.model.user.online

interface OnlineUserCountRepository {
    suspend fun find(accountId: Long): Result<OnlineUserCountResult>
}

sealed interface OnlineUserCountResult {
    object Unknown : OnlineUserCountResult
    data class Success(val count: Int) : OnlineUserCountResult
}