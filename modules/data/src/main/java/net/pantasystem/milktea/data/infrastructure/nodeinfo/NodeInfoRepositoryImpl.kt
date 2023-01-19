package net.pantasystem.milktea.data.infrastructure.nodeinfo

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.activitypub.NodeInfoDTO
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.NodeInfoAPIBuilder
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoDao
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoRecord
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import javax.inject.Inject

class NodeInfoRepositoryImpl @Inject constructor(
    private val nodeInfoAPIBuilder: NodeInfoAPIBuilder,
    private val cache: NodeInfoCache,
    private val nodeInfoDao: NodeInfoDao,
    @IODispatcher val ioDispatcher: CoroutineDispatcher,
): NodeInfoRepository {

    override suspend fun find(host: String): Result<NodeInfo> = runCancellableCatching {
        withContext(ioDispatcher) {
            val inCache = cache.get(host)
            if (inCache != null) {
                return@withContext inCache
            }

            val inDb = nodeInfoDao.find(host)
            if (inDb != null) {
                return@withContext inDb.toModel().also {
                    cache.put(host, it)
                }
            }

            val fetched = requireNotNull(fetch(host)).toModel(host)
            upInsert(fetched)
            cache.put(host, fetched)
            fetched

        }

    }

    override fun get(host: String): NodeInfo? {
        return cache.get(host)
    }

    override fun observe(host: String): Flow<NodeInfo?> {
        return nodeInfoDao.observe(host).map {
            it?.toModel()
        }.onEach {
            if (it != null) {
                cache.put(host, it)
            }
        }
    }

    override suspend fun sync(host: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val nodeInfo = requireNotNull(fetch(host)).toModel(host)
            cache.put(host, nodeInfo)
            upInsert(nodeInfo)
        }
    }

    private suspend fun upInsert(nodeInfo: NodeInfo) {
        val exitInfo = nodeInfoDao.find(nodeInfo.host)
        if (exitInfo == null) {
            nodeInfoDao.insert(NodeInfoRecord.from(nodeInfo))
        } else {
            nodeInfoDao.update(NodeInfoRecord.from(nodeInfo))
        }
    }

    private suspend fun fetch(host: String): NodeInfoDTO? {
        try {
            val nodeInfoUrl = nodeInfoAPIBuilder.buildWellKnown("https://$host")
                .getWellKnownNodeInfo()
                .throwIfHasError()
                .body()?.links?.firstOrNull {
                    it.rel.contains("2.0")
                }?.href ?: "https://$host/nodeinfo/2.0"
            Log.d("NodeInfoRepositoryImpl", "nodeInfoUrl:$nodeInfoUrl")
            return nodeInfoAPIBuilder.build().getNodeInfo(nodeInfoUrl).throwIfHasError().body()
        } catch(e: Throwable) {
            Log.e("NodeInfoRepositoryImpl", "error", e)
            throw e
        }
    }

}

fun NodeInfoDTO.toModel(host: String): NodeInfo {
    return NodeInfo(
        host = host,
        version = version,
        software = NodeInfo.Software(
            name = software.name,
            version = software.version
        )
    )
}

