package net.pantasystem.milktea.api.misskey.infos

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.activitypub.NodeInfoDTO
import net.pantasystem.milktea.model.instance.Meta

@kotlinx.serialization.Serializable
data class InstanceInfosResponse(
    val date: Instant,
    val instancesInfos: List<InstanceInfo>
) {

    @kotlinx.serialization.Serializable
    data class Stats(
        val notesCount: Long,
        val usersCount: Long,
        val mau: Long,
        val instancesCount: Long
    )

    @kotlinx.serialization.Serializable
    data class InstanceInfo(
        val url: String,
        val value: Double,
        val meta: Meta,
        @SerialName("nodeinfo") val nodeInfo: NodeInfoDTO,
        val name: String,
        val description: String? = null,
//        val langs: Map<String, String?>,
        val isAlive: Boolean,
        val banner: Boolean,
        val icon: Boolean,
        val background: Boolean
    )
}