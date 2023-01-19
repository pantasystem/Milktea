package net.pantasystem.milktea.data.infrastructure.nodeinfo

import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeInfoCache @Inject constructor() {
    private val hostAndNodeInfo = mutableMapOf<String, NodeInfo>()

    fun put(host: String, info: NodeInfo) {
        synchronized(hostAndNodeInfo) {
            hostAndNodeInfo[host] = info
        }
    }

    fun get(host: String): NodeInfo? {
        return synchronized(hostAndNodeInfo) {
            hostAndNodeInfo[host]
        }
    }

}