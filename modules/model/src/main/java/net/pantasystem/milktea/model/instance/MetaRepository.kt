package net.pantasystem.milktea.model.instance

import kotlinx.coroutines.flow.Flow

interface MetaRepository {
    suspend fun sync(instanceDomain: String): Result<Unit>

    fun observe(instanceDomain: String): Flow<Meta?>
    suspend fun find(instanceDomain: String): Result<Meta>
}