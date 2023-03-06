package net.pantasystem.milktea.model.markers

import kotlinx.datetime.Instant

interface MarkerRepository {

    fun find(accountId: Long, types: List<MarkerType>): Result<Markers>

    fun save(accountId: Long, params: SaveMarkerParams)

}

enum class MarkerType {
    Home, Notifications,
}

data class Markers(
    val home: Marker?,
    val notifications: Marker?
)

data class Marker(
    val latestReadId: String,
    val version: Long,
    val updatedAt: Instant
)

data class SaveMarkerParams(
    val home: String? = null,
    val notifications: String? = null,
)