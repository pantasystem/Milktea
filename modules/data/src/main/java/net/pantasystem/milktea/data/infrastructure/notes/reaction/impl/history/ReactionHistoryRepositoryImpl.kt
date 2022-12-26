package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryCount
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import javax.inject.Inject

class ReactionHistoryRepositoryImpl @Inject constructor(
    private val reactionHistoryDao: ReactionHistoryDao
): ReactionHistoryRepository {

    override suspend fun create(reactionHistory: ReactionHistory): Result<Unit> = runCancellableCatching {
        withContext(Dispatchers.IO) {
            reactionHistoryDao.insert(ReactionHistoryRecord.from(reactionHistory))
        }
    }

    override suspend fun findAll(): List<ReactionHistory> {
        return withContext(Dispatchers.IO) {
            (reactionHistoryDao.findAll() ?: emptyList()).map {
                it.toHistory()
            }
        }
    }

    override fun observeSumReactions(instanceDomain: String): Flow<List<ReactionHistoryCount>> {
        return reactionHistoryDao.observeSumReactions(instanceDomain).map { list ->
            list.map {
                it.toReactionHistoryCount()
            }
        }
    }

    override suspend fun sumReactions(instanceDomain: String): List<ReactionHistoryCount> {
        return reactionHistoryDao.sumReactions(instanceDomain).map {
            it.toReactionHistoryCount()
        }
    }
}