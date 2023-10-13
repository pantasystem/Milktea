package net.pantasystem.milktea.common.coroutines

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PessimisticCollectiveMutex<Key> {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var locks: Map<Key, Mutex> = mapOf()
    private val lock = Mutex()

    suspend fun<T> withLock(host: Key, block: suspend () -> T): T {
        val l = lock.withLock {
            var l = locks[host]
            if (l == null) {
                l = Mutex()
                locks = locks + (host to l)
            }
            l
        }
        return l.withLock {
            block()
        }
    }

}