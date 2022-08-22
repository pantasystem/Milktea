package jp.panta.misskeyandroidclient.mfm

class Link(
    override val text: String,
    override val start: Int,
    override val end: Int,
    override val insideStart: Int,
    override val insideEnd: Int,
    val url: String
) : Leaf(){
    override val elementType: ElementType = ElementType.LINK
    override fun toString(): String {
        return "Link(text='$text', url='$url')"
    }
}