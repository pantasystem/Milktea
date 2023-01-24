package jp.panta.misskeyandroidclient.mfm

import net.pantasystem.milktea.common_android.mfm.ElementType
import net.pantasystem.milktea.common_android.mfm.Leaf

class Mention(
    override val start: Int,
    override val end: Int,
    override val insideStart: Int,
    override val insideEnd: Int,
    override val text: String,
    val host: String?
) : Leaf(){
    override val elementType: ElementType = ElementType.MENTION
    override fun toString(): String {
        return "Mention(elementType=$elementType, text=$text, host=$host)"
    }
}