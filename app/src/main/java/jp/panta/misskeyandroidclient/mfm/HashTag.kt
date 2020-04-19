package jp.panta.misskeyandroidclient.mfm

class HashTag(
    override val start: Int,
    override val end: Int,
    override val insideStart: Int,
    override val insideEnd: Int,
    val text: String
) : Element{
    override val elementType: ElementType = ElementType.HASH_TAG
}