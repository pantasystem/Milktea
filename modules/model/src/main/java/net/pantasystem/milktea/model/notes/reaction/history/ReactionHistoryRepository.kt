package net.pantasystem.milktea.model.notes.reaction.history

import kotlinx.coroutines.flow.Flow

interface ReactionHistoryRepository {
    suspend fun create(reactionHistory: ReactionHistory): Result<Unit>
    fun observeSumReactions(instanceDomain: String, limit: Int = 80): Flow<List<ReactionHistoryCount>>
    fun observeRecentlyUsedBy(instanceDomain: String, limit: Int = 20): Flow<List<ReactionHistory>>
    suspend fun sumReactions(instanceDomain: String, limit: Int = 80): List<ReactionHistoryCount>
    suspend fun findAll(): List<ReactionHistory>
}