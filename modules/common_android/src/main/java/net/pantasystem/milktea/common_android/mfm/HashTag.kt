package jp.panta.misskeyandroidclient.mfm

import net.pantasystem.milktea.common_android.mfm.ElementType
import net.pantasystem.milktea.common_android.mfm.Leaf

class HashTag(
    override val start: Int,
    override val end: Int,
    override val insideStart: Int,
    override val insideEnd: Int,
    override val text: String
) : Leaf(){
    override val elementType: ElementType = ElementType.HASH_TAG
    override fun toString(): String {
        return "HashTag(elementType=$elementType, tag=$text)"
    }
}