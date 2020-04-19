package jp.panta.misskeyandroidclient.mfm

import jp.panta.misskeyandroidclient.model.emoji.Emoji

class EmojiElement(
    val emoji: Emoji,
    override val text: String,
    override val start: Int,
    override val end: Int,
    override val insideStart: Int,
    override val insideEnd: Int
) : Leaf(){
    override val elementType: ElementType = ElementType.EMOJI
    override fun toString(): String {
        return "EmojiElement(elementType=$elementType, emoji=:${emoji.name}:)"
    }
}