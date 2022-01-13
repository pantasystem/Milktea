package jp.panta.misskeyandroidclient.util

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

fun<T> Mutex.blockingWithLockWithCheckTimeout(owner: Any? = null, timeMillis: Long = 100, action: () -> T): T {
    return runBlocking {
        withTimeout(timeMillis) {
            withLock(owner = owner, action = action)
        }
    }
}