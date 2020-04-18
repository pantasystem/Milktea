package jp.panta.misskeyandroidclient.mfm

class Text(
    val text: String,
    override val start: Int,
    override val end: Int = start + text.length,
    override val insideStart: Int = start,
    override val insideEnd: Int = end
) : Element{
    override val elementType: ElementType = ElementType.TEXT

    override fun toString(): String {
        return text
    }
}