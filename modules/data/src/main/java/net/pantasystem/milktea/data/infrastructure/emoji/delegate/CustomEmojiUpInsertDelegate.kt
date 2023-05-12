package net.pantasystem.milktea.data.infrastructure.emoji.delegate

import androidx.room.Transaction
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiAliasRecord
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiDAO
import net.pantasystem.milktea.data.infrastructure.emoji.db.toRecord
import net.pantasystem.milktea.model.emoji.Emoji
import javax.inject.Inject

internal class CustomEmojiUpInsertDelegate @Inject constructor(
    private val customEmojiDAO: CustomEmojiDAO,
) {

    @Transaction
    suspend operator fun invoke(host: String, emojis: List<Emoji>) {
        val record = emojis.map {
            it.toRecord(host)
        }
        val exists = customEmojiDAO.findBy(host).associateBy {
            it.emoji.name
        }

        val insertResults = customEmojiDAO.insertAll(record)

        val removedEmojis = emojis.mapNotNull {
            exists[it.name]?.emoji
        }
        customEmojiDAO.deleteBy(removedEmojis)

        val ids = insertResults.mapIndexed { index, id ->
            if (id == -1L) {
                customEmojiDAO.findBy(host, emojis[index].name).firstOrNull()?.emoji?.id ?: -1
            } else {
                id
            }
        }

        customEmojiDAO.deleteAliasByEmojiIds(ids.filterNot { it == -1L })
        val aliasRecords = emojis.mapIndexedNotNull { index, emoji ->
            val id = ids[index]
            if (id == -1L) {
                null
            } else {
                emoji.aliases?.filterNot {
                    it.isBlank()
                }?.map { alias ->
                    CustomEmojiAliasRecord(
                        emojiId = id,
                        value = alias
                    )
                }
            }
        }.flatten()
        aliasRecords.chunked(500).map {
            customEmojiDAO.insertAliases(it)
        }
    }
}