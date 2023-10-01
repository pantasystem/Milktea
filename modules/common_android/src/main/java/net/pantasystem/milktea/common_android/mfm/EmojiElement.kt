package jp.panta.misskeyandroidclient.mfm

import net.pantasystem.milktea.common_android.mfm.ElementType
import net.pantasystem.milktea.common_android.mfm.Leaf
import net.pantasystem.milktea.model.emoji.CustomEmoji

class EmojiElement(
    val emoji: CustomEmoji,
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