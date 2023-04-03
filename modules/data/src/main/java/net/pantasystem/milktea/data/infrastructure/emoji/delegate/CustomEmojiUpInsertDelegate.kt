package net.pantasystem.milktea.data.infrastructure.emoji.delegate

import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiAliasRecord
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiDAO
import net.pantasystem.milktea.data.infrastructure.emoji.db.toRecord
import net.pantasystem.milktea.model.emoji.Emoji
import javax.inject.Inject

internal class CustomEmojiUpInsertDelegate @Inject constructor(
    private val customEmojiDAO: CustomEmojiDAO,
) {
    suspend operator fun invoke(host: String, emojis: List<Emoji>) {
        val record = emojis.map {
            it.toRecord(host)
        }
        val ids = customEmojiDAO.insertAll(record)

        ids.mapIndexed { index, id ->
            if (id == -1L) {
                customEmojiDAO.findBy(host, emojis[index].name).firstOrNull()?.let { record ->
                    customEmojiDAO.update(emojis[index].toRecord(host, record.emoji.id))
                    customEmojiDAO.deleteAliasByEmojiId(record.emoji.id)
                    emojis[index].aliases?.map {
                        CustomEmojiAliasRecord(
                            emojiId = record.emoji.id,
                            it
                        )
                    }?.let {
                        customEmojiDAO.insertAliases(it)
                    }
                    record.emoji.id
                }
            } else {
                emojis[index].aliases?.filterNot {
                    it.isBlank()
                }?.map {
                    CustomEmojiAliasRecord(
                        emojiId = id,
                        it
                    )
                }?.let {
                    customEmojiDAO.insertAliases(it)
                }
                id
            }
        }
    }
}