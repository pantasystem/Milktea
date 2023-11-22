package net.pantasystem.milktea.model.statistics

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface LastlyInAppReviewShownRepository {
    fun observe(): Flow<Instant?>

    suspend fun get(): Result<Instant?>

    suspend fun set(time: Instant): Result<Unit>

    suspend fun clear(): Result<Unit>
}