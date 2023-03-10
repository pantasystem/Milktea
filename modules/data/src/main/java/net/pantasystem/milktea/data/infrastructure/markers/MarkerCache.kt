package net.pantasystem.milktea.data.infrastructure.markers

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.model.markers.MarkerType
import net.pantasystem.milktea.model.markers.Markers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkerCache @Inject constructor() {

    private var markersMap = mapOf<Key, Markers>()
    private val mutex = Mutex()

    suspend fun put(key: Key, markers: Markers) {
        mutex.withLock {
            markersMap = markersMap.toMutableMap().also {
                it[key] = markers
            }
        }
    }

    fun get(key: Key): Markers? {
        return markersMap[key]
    }

    data class Key(
        val accountId: Long,
        val types: List<MarkerType>
    )
}