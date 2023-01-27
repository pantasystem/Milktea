package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * InMemoryなMetaのCache
 */
@Singleton
class MastodonInstanceInfoCache @Inject constructor(){

    private val lock = Mutex()
    private var map = mapOf<String, MastodonInstanceInfo>()

    suspend fun put(instanceBaseURL: String, instanceInfo: MastodonInstanceInfo) {
        lock.withLock {
            map = map.toMutableMap().also { m ->
                m[instanceBaseURL] = instanceInfo
            }
        }
    }

    fun get(instanceBaseURL: String): MastodonInstanceInfo? {
        return map[instanceBaseURL]
    }
}