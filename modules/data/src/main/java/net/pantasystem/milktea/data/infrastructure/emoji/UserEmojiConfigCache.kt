package net.pantasystem.milktea.data.infrastructure.emoji

import net.pantasystem.milktea.model.emoji.UserEmojiConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserEmojiConfigCache @Inject constructor() {

    private var cache = mutableMapOf<String, List<UserEmojiConfig>>()

    fun put(instanceDomain: String, config: List<UserEmojiConfig>) {
        synchronized(cache) {
            cache[instanceDomain] = config
        }
    }

    fun get(instanceDomain: String): List<UserEmojiConfig>? {
        return cache[instanceDomain]
    }
}