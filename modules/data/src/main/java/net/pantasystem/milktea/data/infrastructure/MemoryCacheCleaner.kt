package net.pantasystem.milktea.data.infrastructure

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryCacheCleaner @Inject constructor(){
    interface Cleanable {
        suspend fun clean()
    }

    private var cleanableCacheList = mutableListOf<Cleanable>()

    fun register(cleanable: Cleanable) {
        synchronized(this) {
            cleanableCacheList.add(cleanable)
        }
    }

    suspend fun clean() {
        cleanableCacheList.forEach {
            it.clean()
        }
    }
}