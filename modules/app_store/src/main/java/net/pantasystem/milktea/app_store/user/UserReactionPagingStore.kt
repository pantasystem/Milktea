package net.pantasystem.milktea.app_store.user

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.reaction.UserReactionRelation

interface UserReactionPagingStore {
    interface Factory {
        fun create(userId: User.Id): UserReactionPagingStore
    }

    val state: Flow<PageableState<List<UserReactionRelation>>>

    suspend fun loadPrevious(): Result<Unit>
    suspend fun clear(): Result<Unit>

}
