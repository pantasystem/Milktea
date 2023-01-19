package net.pantasystem.milktea.model.nodeinfo

import kotlinx.coroutines.flow.Flow

interface NodeInfoRepository {
    suspend fun find(host: String): Result<NodeInfo>
    suspend fun sync(host: String): Result<Unit>
    suspend fun syncAll(): Result<Unit>
    fun get(host: String): NodeInfo?
    fun observe(host: String): Flow<NodeInfo?>
}