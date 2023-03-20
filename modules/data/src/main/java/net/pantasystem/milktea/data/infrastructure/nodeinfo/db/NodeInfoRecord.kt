package net.pantasystem.milktea.data.infrastructure.nodeinfo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.nodeinfo.NodeInfo

@Entity(
    tableName = "nodeinfo",
)
data class NodeInfoRecord(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "host")
    val host: String,

    @ColumnInfo(name = "nodeInfoVersion")
    val nodeInfoVersion: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "version")
    val version: String
) {

    companion object {
        fun from(nodeInfo: NodeInfo): NodeInfoRecord {
            return NodeInfoRecord(
                host = nodeInfo.host,
                nodeInfoVersion = nodeInfo.version,
                version = nodeInfo.software.version,
                name = nodeInfo.software.name,
            )
        }
    }

    fun toModel(): NodeInfo {
        return NodeInfo(
            version = nodeInfoVersion,
            host = host,
            software = NodeInfo.Software(
                version = version,
                name = name,
            )
        )
    }
}

