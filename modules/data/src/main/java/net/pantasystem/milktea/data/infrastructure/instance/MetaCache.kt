package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.model.instance.Meta
import javax.inject.Inject
import javax.inject.Singleton

/**
 * InMemoryなMetaのCache
 */
@Singleton
class MetaCache @Inject constructor(){

    private val lock = Mutex()
    private var map = mapOf<String, Meta>()

    suspend fun put(instanceBaseURL: String, meta: Meta) {
        lock.withLock {
            map = map.toMutableMap().also { m ->
                m[instanceBaseURL] = meta
            }
        }
    }

    fun get(instanceBaseURL: String): Meta? {
        return map[instanceBaseURL]
    }
}