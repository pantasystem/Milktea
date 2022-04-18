package jp.panta.misskeyandroidclient.ui.emojis.viewmodel

import net.pantasystem.milktea.model.emoji.Emoji

sealed class Emojis : IEmoji {
    data class TextEmoji(
        val text: String
    ): Emojis()

    data class CustomEmoji(
        val emoji: net.pantasystem.milktea.model.emoji.Emoji
    ) : Emojis()

    data class EmojiCategory(val categoryName: String) : Emojis()

    companion object{
        @JvmStatic
        fun categoryBy(emojis: List<net.pantasystem.milktea.model.emoji.Emoji>): List<Emojis>{
            val list = ArrayList<Emojis>()
            emojis.groupBy {
                it.category
            }.forEach {
                val c = it.key
                c?.let{
                    list.add(EmojiCategory(c))
                }
                list.addAll(
                    it.value.map{ emoji ->
                        CustomEmoji(emoji)
                    }
                )
            }
            return list
        }
    }

}