package net.pantasystem.milktea.api.activitypub

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class NodeInfoDTO(
    @SerialName("version")
    val version: String,

    @SerialName("software")
    val software: SoftwareDTO
) {
    @kotlinx.serialization.Serializable
    data class SoftwareDTO(
        @SerialName("name")
        val name: String,

        @SerialName("version")
        val version: String,
    )
}