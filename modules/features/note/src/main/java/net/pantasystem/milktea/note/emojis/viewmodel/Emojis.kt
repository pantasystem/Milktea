package net.pantasystem.milktea.note.emojis.viewmodel

import net.pantasystem.milktea.model.emoji.Emoji

sealed class Emojis : IEmoji {
    data class TextEmoji(
        val text: String
    ): Emojis()

    data class CustomEmoji(
        val emoji: Emoji
    ) : Emojis()

    data class EmojiCategory(val categoryName: String) : Emojis()

    companion object{
        @JvmStatic
        fun categoryBy(emojis: List<Emoji>): List<Emojis>{
            return emojis.groupBy {
                it.category
            }.map {
                val c = it.key
                listOfNotNull(
                    c?.let {
                        EmojiCategory(c)
                    },

                ) + it.value.map { emoji ->
                    CustomEmoji(emoji)
                }
            }.flatten()
        }
    }

}