package net.pantasystem.milktea.data.infrastructure.instance

import net.pantasystem.milktea.common.collection.LRUCache
import net.pantasystem.milktea.model.instance.Meta
import javax.inject.Inject
import javax.inject.Singleton

/**
 * InMemoryなMetaのCache
 */
@Singleton
class MetaCache @Inject constructor(){

    private val lruCache = LRUCache<String, Meta>(100)

    fun put(instanceBaseURL: String, meta: Meta) {
        lruCache[instanceBaseURL] = meta
    }

    fun get(instanceBaseURL: String): Meta? {
        return lruCache[instanceBaseURL]
    }
}