package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryCount
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import javax.inject.Inject

class ReactionHistoryRepositoryImpl @Inject constructor(
    private val reactionHistoryDao: ReactionHistoryDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
): ReactionHistoryRepository {

    override suspend fun create(reactionHistory: ReactionHistory): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            reactionHistoryDao.insert(ReactionHistoryRecord.from(reactionHistory))
        }
    }

    override suspend fun findAll(): List<ReactionHistory> {
        return withContext(ioDispatcher) {
            (reactionHistoryDao.findAll() ?: emptyList()).map {
                it.toHistory()
            }
        }
    }

    override fun observeSumReactions(instanceDomain: String, limit: Int): Flow<List<ReactionHistoryCount>> {
        return reactionHistoryDao.observeSumReactions(instanceDomain, limit).map { list ->
            list.map {
                it.toReactionHistoryCount()
            }
        }
    }

    override suspend fun sumReactions(instanceDomain: String, limit: Int): List<ReactionHistoryCount> {
        return withContext(ioDispatcher) {
            reactionHistoryDao.sumReactions(instanceDomain, limit).map {
                it.toReactionHistoryCount()
            }
        }
    }

    override fun observeRecentlyUsedBy(instanceDomain: String, limit: Int): Flow<List<ReactionHistory>> {
        return reactionHistoryDao.observeRecentlyUsed(instanceDomain, limit).map { records ->
            records.map { history ->
                ReactionHistory(
                    instanceDomain = history.instanceDomain,
                    reaction = history.reaction,
                    id = history.id
                )
            }
        }.flowOn(Dispatchers.IO)
    }
}