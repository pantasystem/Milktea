package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiInserter @Inject constructor(
    private val customEmojiDAO: CustomEmojiDAO,
) {

    @VisibleForTesting
    var locks: Map<String, Mutex> = mapOf()
    private val lock = Mutex()

    suspend fun replaceAll(host: String, emojis: List<CustomEmojiRecord>): List<CustomEmojiRecord> {
        return inLock(host) {
            customEmojiDAO.deleteByHost(host)
            val inserted = emojis.chunked(500).map { chunkedEmojis ->
                val ids = customEmojiDAO.insertAll(chunkedEmojis)
                chunkedEmojis.mapIndexed { index, customEmojiRecord ->
                    customEmojiRecord.copy(id = ids[index])
                }
            }.flatten()
            inserted
        }
    }

    @VisibleForTesting
    suspend fun<T> inLock(host: String, block: suspend () -> T): T {
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