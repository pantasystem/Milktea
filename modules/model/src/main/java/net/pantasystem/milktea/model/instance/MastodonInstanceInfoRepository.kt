package net.pantasystem.milktea.model.instance

import kotlinx.coroutines.flow.Flow

interface MastodonInstanceInfoRepository {

    suspend fun sync(instanceDomain: String): Result<Unit>

    fun observe(instanceDomain: String): Flow<MastodonInstanceInfo?>

    fun get(instanceDomain: String): MastodonInstanceInfo?

    suspend fun find(instanceDomain: String): Result<MastodonInstanceInfo>
}