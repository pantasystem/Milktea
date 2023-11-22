package net.pantasystem.milktea.model.statistics

import kotlinx.coroutines.flow.Flow

interface InAppPostCounterRepository {
    fun observe(): Flow<Int>

    suspend fun get(): Result<Int>

    suspend fun increment(): Result<Unit>

    suspend fun clear(): Result<Unit>
}