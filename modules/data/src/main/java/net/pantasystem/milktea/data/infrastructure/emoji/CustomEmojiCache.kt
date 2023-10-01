package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.model.emoji.CustomEmoji
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiCache @Inject constructor() {
    private val lock = Mutex()
    private var map = mapOf<String, List<CustomEmoji>>()
    private var maps = mapOf<String, Map<String, CustomEmoji>>()

    suspend fun put(host: String, emojis: List<CustomEmoji>) {
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

    fun get(host: String): List<CustomEmoji>? {
        return map[host]
    }

    fun getMap(host: String): Map<String, CustomEmoji>? {
        return maps[host]
    }
}