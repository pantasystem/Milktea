package net.pantasystem.milktea.model.search

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {

    suspend fun add(history: SearchHistory): Result<SearchHistory>

    suspend fun delete(id: Long): Result<Unit>

    fun observeBy(accountId: Long, limit: Int = 10): Flow<List<SearchHistory>>

}