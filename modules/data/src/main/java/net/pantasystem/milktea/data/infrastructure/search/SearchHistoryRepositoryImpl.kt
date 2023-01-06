package net.pantasystem.milktea.data.infrastructure.search

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.search.SearchHistory
import net.pantasystem.milktea.model.search.SearchHistoryRepository
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao,
    @IODispatcher val ioDispatcher: CoroutineDispatcher,
) : SearchHistoryRepository {
    override suspend fun add(history: SearchHistory): Result<SearchHistory> = runCancellableCatching{

        withContext(ioDispatcher) {
            val id = searchHistoryDao.insert(history.toRecord())
            searchHistoryDao.findOne(id)?.toModel() ?: throw IllegalStateException()
        }
    }

    override suspend fun delete(id: Long): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            searchHistoryDao.delete(searchHistoryDao.findOne(id) ?: throw NoSuchElementException("search history not found:${id}"))
        }
    }

    override fun observeBy(accountId: Long, limit: Int): Flow<List<SearchHistory>> {
        return searchHistoryDao.observeByAccountId(accountId, limit).map { records ->
            records.map {
                it.toModel()
            }
        }.flowOn(ioDispatcher)
    }
}