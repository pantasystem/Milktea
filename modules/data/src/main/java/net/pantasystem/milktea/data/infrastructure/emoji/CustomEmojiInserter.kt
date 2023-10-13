package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.model.emoji.EmojiWithAlias
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiInserter @Inject constructor(
    private val customEmojiDAO: CustomEmojiDAO,
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var locks: Map<String, Mutex> = mapOf()
    private val lock = Mutex()


    suspend fun convertAndReplaceAll(host: String, emojis: List<EmojiWithAlias>): List<CustomEmojiRecord> {
        val emojisBeforeInsert = emojis.map {
            CustomEmojiRecord.from(it.emoji, host)
        }
        val inserted = replaceAll(
            host,
            emojisBeforeInsert,
        )
        insertAliases(
            inserted.map {
                it.id
            },
            emojis.map {
                it.aliases ?: emptyList()
            }
        )
        return inserted
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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

    private suspend fun insertAliases(emojiIds: List<Long>, aliases: List<List<String>>) {
        assert(emojiIds.size == aliases.size) {
            "emojis.size != aliases.size"
        }
        emojiIds.asSequence().mapIndexed { index, l ->
            aliases[index].map {
                CustomEmojiAliasRecord(
                    name = it,
                    emojiId = l
                )
            }
        }.flatten().chunked(500).toList().map { chunkedAliases ->
            customEmojiDAO.insertAliases(chunkedAliases)
        }.flatten().toList()

    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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