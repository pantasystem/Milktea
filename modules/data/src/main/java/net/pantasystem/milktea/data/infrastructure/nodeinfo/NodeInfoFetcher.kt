package net.pantasystem.milktea.data.infrastructure.nodeinfo

import android.util.Log
import net.pantasystem.milktea.api.activitypub.NodeInfoDTO
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.NodeInfoAPIBuilder
import javax.inject.Inject

interface NodeInfoFetcher {
    suspend fun fetch(host: String): NodeInfoDTO?
}

class NodeInfoFetcherImpl @Inject constructor(
    private val nodeInfoAPIBuilder: NodeInfoAPIBuilder
): NodeInfoFetcher {

    override suspend fun fetch(host: String): NodeInfoDTO? {
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