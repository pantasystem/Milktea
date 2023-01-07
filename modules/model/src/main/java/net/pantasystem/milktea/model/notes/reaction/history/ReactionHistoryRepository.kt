package net.pantasystem.milktea.model.notes.reaction.history

import kotlinx.coroutines.flow.Flow

interface ReactionHistoryRepository {
    suspend fun create(reactionHistory: ReactionHistory): Result<Unit>
    fun observeSumReactions(instanceDomain: String, limit: Int = 50): Flow<List<ReactionHistoryCount>>
    suspend fun sumReactions(instanceDomain: String, limit: Int = 50): List<ReactionHistoryCount>
    suspend fun findAll(): List<ReactionHistory>
}