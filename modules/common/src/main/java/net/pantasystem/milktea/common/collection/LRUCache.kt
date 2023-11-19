package net.pantasystem.milktea.common.collection

class LRUCache<K, V>(private val capacity: Int) {
    private val cache = linkedMapOf<K, V>()

    operator fun get(key: K): V? {
        synchronized(cache) {
            val value = cache[key]

            if(value != null) {
                cache.remove(key)
                cache[key] = value
            }

            return value
        }
    }

    fun put(key: K, value: V) {
        synchronized(cache) {
            if(get(key) == null) {
                if(cache.size >= capacity) {
                    val firstKey = cache.iterator().next().key
                    cache.remove(firstKey)
                }

                cache[key] = value
            } else {
                cache.remove(key)
                cache[key] = value
            }
        }
    }

    fun remove(key: K) {
        synchronized(cache) {
            cache.remove(key)
        }
    }

    fun clear() {
        synchronized(cache) {
            cache.clear()
        }
    }
}