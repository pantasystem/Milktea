package net.pantasystem.milktea.model.notes.reaction.history

import kotlinx.coroutines.flow.Flow

interface ReactionHistoryRepository {
    suspend fun create(reactionHistory: ReactionHistory): Result<Unit>
    fun observeSumReactions(instanceDomain: String): Flow<List<ReactionHistoryCount>>
    suspend fun sumReactions(instanceDomain: String): List<ReactionHistoryCount>
    suspend fun findAll(): List<ReactionHistory>
}