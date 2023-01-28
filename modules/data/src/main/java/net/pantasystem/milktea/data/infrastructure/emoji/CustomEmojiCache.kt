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

    suspend fun put(host: String, emojis: List<Emoji>) {
        lock.withLock {
            map = map.toMutableMap().also { m ->
                m[host] = emojis
            }
        }
    }

    fun get(host: String): List<Emoji>? {
        return map[host]
    }
}