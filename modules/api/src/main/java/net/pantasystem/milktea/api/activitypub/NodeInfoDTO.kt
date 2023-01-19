package net.pantasystem.milktea.api.activitypub

@kotlinx.serialization.Serializable
data class NodeInfoDTO(
    val version: String,
    val software: SoftwareDTO
) {
    @kotlinx.serialization.Serializable
    data class SoftwareDTO(
        val name: String,
        val version: String,
    )
}