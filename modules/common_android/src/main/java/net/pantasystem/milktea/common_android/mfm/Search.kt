package net.pantasystem.milktea.common_android.mfm

class Search(
    override val text: String,
    override val start: Int,
    override val end: Int,
    override val insideStart: Int,
    override val insideEnd: Int
) : Leaf(){
    override val elementType: ElementType = ElementType.SEARCH
    override fun toString(): String {
        return "Search(elementType=$elementType, text=$text)"
    }
}