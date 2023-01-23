package net.pantasystem.milktea.app_store.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.user.User

sealed interface RequestType {
    companion object
    val userId: User.Id
    data class Follower(override val userId: User.Id) : RequestType
    data class Following(override val userId: User.Id) : RequestType
}

fun RequestType.string(): String {
    return when(this) {
        is RequestType.Following -> "following"
        is RequestType.Follower -> "follower"
    }
}

fun RequestType.Companion.from(type: String, userId: User.Id): RequestType {
    return when(type) {
        "following" -> {
            RequestType.Following(userId)
        }
        "follower" -> {
            RequestType.Follower(userId)
        }
        else -> RequestType.Following(userId)
    }
}
interface FollowFollowerPagingStore {

    interface Factory {
        fun create(type: RequestType): FollowFollowerPagingStore
    }

    val type: RequestType

    val state: Flow<PageableState<List<User.Id>>>
    val users: Flow<List<User.Detail>>

    suspend fun loadPrevious()
    suspend fun clear()
}