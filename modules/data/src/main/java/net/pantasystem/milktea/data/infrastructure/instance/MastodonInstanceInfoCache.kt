package net.pantasystem.milktea.data.infrastructure.instance

import net.pantasystem.milktea.common.collection.LRUCache
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * InMemoryなMetaのCache
 */
@Singleton
class MastodonInstanceInfoCache @Inject constructor(){

    private val lruCache = LRUCache<String, MastodonInstanceInfo>(100)

    fun put(instanceBaseURL: String, instanceInfo: MastodonInstanceInfo) {
        lruCache[instanceBaseURL] = instanceInfo
    }

    fun get(instanceBaseURL: String): MastodonInstanceInfo? {
        return lruCache[instanceBaseURL]
    }
}