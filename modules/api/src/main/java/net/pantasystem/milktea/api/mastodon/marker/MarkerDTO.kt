package net.pantasystem.milktea.api.mastodon.marker

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.markers.Marker

@kotlinx.serialization.Serializable
data class MarkerDTO(
    @SerialName("last_read_id") val lastReadId: String,
    @SerialName("version") val version: Long,
    @SerialName("updated_at") val updatedAt: Instant
) {
    fun toModel(): Marker {
        return Marker(
            lastReadId = lastReadId,
            version = version,
            updatedAt = updatedAt
        )
    }
}

@kotlinx.serialization.Serializable
data class MarkersDTO(
    val home: MarkerDTO? = null,
    val notifications: MarkerDTO? = null,
)

@kotlinx.serialization.Serializable
data class SaveMarkersRequest(
    val home: String? = null,
    val notifications: String? = null,
)