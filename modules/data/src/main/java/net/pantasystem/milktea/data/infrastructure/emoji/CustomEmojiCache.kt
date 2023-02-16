package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.model.emoji.Emoji
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiCache @Inject constructor() {
    private val lock = Mutex()
    private var map = mapOf<String, List<Emoji>>()
    private var maps = mapOf<String, Map<String, Emoji>>()

    suspend fun put(host: String, emojis: List<Emoji>) {
        lock.withLock {
            map = map.toMutableMap().also { m ->
                m[host] = emojis
            }
            maps = maps.toMutableMap().also { m ->
                m[host] = emojis.associateBy {
                    it.name
                }
            }
        }
    }

    fun get(host: String): List<Emoji>? {
        return map[host]
    }

    fun getMap(host: String): Map<String, Emoji>? {
        return maps[host]
    }
}