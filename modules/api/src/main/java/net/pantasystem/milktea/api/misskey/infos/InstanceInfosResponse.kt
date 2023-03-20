package net.pantasystem.milktea.api.misskey.infos

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.activitypub.NodeInfoDTO
import net.pantasystem.milktea.model.instance.Meta

@kotlinx.serialization.Serializable
data class InstanceInfosResponse(
    @SerialName("date")
    val date: Instant,

    @SerialName("instanceInfos")
    val instancesInfos: List<InstanceInfo>
) {

//    @kotlinx.serialization.Serializable
//    data class Stats(
//        val notesCount: Long,
//        val usersCount: Long,
//        val mau: Long,
//        val instancesCount: Long
//    )

    @kotlinx.serialization.Serializable
    data class InstanceInfo(
        @SerialName("url")
        val url: String,

        @SerialName("value")
        val value: Double,

        @SerialName("meta")
        val meta: Meta,

        @SerialName("nodeinfo") val nodeInfo: NodeInfoDTO,

        @SerialName("name")
        val name: String,

        @SerialName("description")
        val description: String? = null,
//        val langs: Map<String, String?>,
        @SerialName("isAlive")
        val isAlive: Boolean,

        @SerialName("banner")
        val banner: Boolean,

        @SerialName("icon")
        val icon: Boolean,

        @SerialName("background")
        val background: Boolean
    )
}