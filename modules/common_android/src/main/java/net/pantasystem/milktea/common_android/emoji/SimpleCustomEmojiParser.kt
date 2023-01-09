package net.pantasystem.milktea.common_android.emoji

import net.pantasystem.milktea.model.emoji.Emoji

object CustomEmojiParser {


    fun parse(accountHost: String?, sourceHost: String?, emojis: List<Emoji>?, text: String): CustomEmojiParsedResult {
        val emojiMap = emojis?.associateBy {
            it.name
        }
        var cursor = 0
        var colonIndex = -1
        val emojiPosList = mutableListOf<EmojiPos>()
        while(cursor < text.length) {
            if (text[cursor] == ':') {
                if (colonIndex == -1) {
                    colonIndex = cursor
                } else {
                    val subStr = text.substring(colonIndex, cursor + 1)
                    val tag = if (subStr.length > 2) {
                        subStr.substring(1, subStr.length - 1)
                    } else {
                        null
                    }
                    var emoji = emojiMap?.get(tag)
                    if (emoji == null) {
                        if (accountHost != null && sourceHost != null) {
                            if (tag != null){
                                emoji = Emoji(
                                    name = tag,
                                    uri = V13EmojiUrlResolver.resolve(accountHost = accountHost, emojiHost = sourceHost, tagName = tag),
                                    url = V13EmojiUrlResolver.resolve(accountHost = accountHost, emojiHost = sourceHost, tagName = tag)
                                )
                            }
                        }

                    }
                    colonIndex = if (emoji == null) {
                        cursor
                    } else {
                        emojiPosList.add(EmojiPos(
                            emoji = emoji,
                            start = colonIndex,
                            end = cursor + 1
                        ))
                        -1
                    }
                }
            }
            cursor ++
        }
        return CustomEmojiParsedResult(text, emojiPosList)
    }
}

data class CustomEmojiParsedResult(
    val text: String,
    val emojis: List<EmojiPos>,
)

data class EmojiPos(val start: Int, val end: Int, val emoji: Emoji)