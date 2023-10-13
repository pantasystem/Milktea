package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.annotation.VisibleForTesting
import net.pantasystem.milktea.common.coroutines.PessimisticCollectiveMutex
import net.pantasystem.milktea.model.emoji.EmojiWithAlias
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiInserter @Inject constructor(
    private val customEmojiDAO: CustomEmojiDAO,
) {
    private val mutex = PessimisticCollectiveMutex<String>()

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
        return mutex.withLock(host) {
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

}