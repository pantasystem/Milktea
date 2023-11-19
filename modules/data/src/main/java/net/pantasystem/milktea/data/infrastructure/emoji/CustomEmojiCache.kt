package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.collection.LRUCache
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.model.emoji.CustomEmoji
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiCache @Inject constructor() : MemoryCacheCleaner.Cleanable {
    private val lock = Mutex()
    private val mappedCache = LRUCache<String, Map<String, CustomEmoji>>(3)
    private val listCache = LRUCache<String, List<CustomEmoji>>(3)

    suspend fun put(host: String, emojis: List<CustomEmoji>) {
        lock.withLock {
            mappedCache.put(host, emojis.associateBy {
                it.name
            })
            listCache.put(host, emojis)
        }
    }

    fun get(host: String): List<CustomEmoji>? {
        return listCache[host]
    }

    fun getMap(host: String): Map<String, CustomEmoji>? {
        return mappedCache[host]
    }

    override suspend fun clean() {
        lock.withLock {
            mappedCache.clear()
            listCache.clear()
        }
    }

}