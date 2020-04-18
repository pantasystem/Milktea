package jp.panta.misskeyandroidclient.mfm

class Search(
    val text: String,
    override val start: Int,
    override val end: Int,
    override val insideStart: Int,
    override val insideEnd: Int
) : Element{
    override val elementType: ElementType = ElementType.SEARCH
    override fun toString(): String {
        return "Search(elementType=$elementType, text=$text)"
    }
}