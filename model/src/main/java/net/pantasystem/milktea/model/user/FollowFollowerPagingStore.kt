package net.pantasystem.milktea.model.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.pantasystem.milktea.common.PageableState

sealed interface RequestType {
    val userId: User.Id
    data class Follower(override val userId: User.Id) : RequestType
    data class Following(override val userId: User.Id) : RequestType
}
interface FollowFollowerPagingStore {

    interface Factory {
        fun create(type: RequestType): FollowFollowerPagingStore
    }

    val type: RequestType

    val state: StateFlow<PageableState<List<User.Id>>>
    val users: Flow<List<User>>

    suspend fun loadPrevious()
    suspend fun clear()
}