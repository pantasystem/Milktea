package jp.panta.misskeyandroidclient.mfm

import net.pantasystem.milktea.common_android.mfm.ElementType
import net.pantasystem.milktea.common_android.mfm.Leaf

class Text(
    override val text: String,
    override val start: Int,
    override val end: Int = start + text.length,
    override val insideStart: Int = start,
    override val insideEnd: Int = end
) : Leaf(){
    override val elementType: ElementType = ElementType.TEXT

    override fun toString(): String {
        return text
    }
}