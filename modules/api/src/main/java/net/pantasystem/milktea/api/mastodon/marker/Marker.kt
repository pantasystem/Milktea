package net.pantasystem.milktea.api.mastodon.marker

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Marker(
    @SerialName("last_read_id") val lastReadId: String,
    @SerialName("version") val version: Long,
    @SerialName("Updated_at") val updatedAt: Instant
)

@kotlinx.serialization.Serializable
data class Markers(
    val home: Marker? = null,
    val notifications: Marker? = null,
)

@kotlinx.serialization.Serializable
data class SaveMarkers(
    val home: String? = null,
    val notifications: String? = null,
)