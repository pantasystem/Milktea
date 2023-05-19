package net.pantasystem.milktea.model.emoji

object CustomEmojiParser {


    fun parse(sourceHost: String?, emojis: List<Emoji>?, text: String, instanceEmojis: Map<String, Emoji>? = null): CustomEmojiParsedResult {
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
                    var emoji: EmojiResolvedType? = (emojiMap?.get(tag) ?: instanceEmojis?.get(tag))?.let {
                        if (sourceHost == null) {
                            null
                        } else {
                            EmojiResolvedType.Resolved(it, sourceHost)
                        }
                    }
                    if (emoji == null) {
                        if (sourceHost != null) {
                            if (tag != null){
                                emoji = EmojiResolvedType.UnResolved(
                                    tag = tag,
                                    sourceHost = sourceHost,
                                )
                            }
                        }

                    }
                    colonIndex = if (emoji == null) {
                        cursor
                    } else {
                        emojiPosList.add(
                            EmojiPos(
                            result = emoji,
                            start = colonIndex,
                            end = cursor + 1
                        )
                        )
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

sealed interface EmojiResolvedType {
    data class Resolved(val emoji: Emoji, val sourceHost: String) : EmojiResolvedType {
        override val tag: String = emoji.name
    }
    data class UnResolved(override val tag: String, val sourceHost: String) : EmojiResolvedType

    fun getUrl(accountHost: String?): String? {
        return when(this) {
            is Resolved -> {
                emoji.url ?: emoji.uri
                ?: resolve(accountHost = accountHost, emojiHost = sourceHost, tagName = emoji.name)
            }
            is UnResolved -> {
                return resolve(accountHost = accountHost, emojiHost = sourceHost, tagName = tag)
            }
        }
    }

    val tag: String


}

/**
 * MFMParserからの使用を想定
 */
private fun resolve(
    accountHost: String?,
    tagName: String,
    emojiHost: String?,
): String {
    if (emojiHost == null || accountHost == emojiHost) {
        return "https://$accountHost/emoji/${tagName}.webp"
    }
    return "https://$accountHost/emoji/${tagName}@${emojiHost}.webp"
}

data class EmojiPos(val start: Int, val end: Int, val result: EmojiResolvedType)