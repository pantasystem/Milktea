package net.pantasystem.milktea.model.filter

import kotlinx.coroutines.flow.Flow

interface MastodonWordFilterRepository {

    suspend fun sync(accountId: Long): Result<Unit>

    suspend fun findAll(accountId: Long): Result<List<MastodonWordFilter>>

    suspend fun observeAll(accountId: Long): Flow<List<MastodonWordFilter>>

}