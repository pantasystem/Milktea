package jp.panta.misskeyandroidclient.mfm

class Root(
    val sourceText: String
) : Node(0, sourceText.length, 0, sourceText.length, ElementType.ROOT, null){
    override val elementType: ElementType = ElementType.ROOT
    override val start: Int = 0
    override val end: Int = sourceText.length
    override val insideStart: Int = 0
    override val insideEnd: Int = sourceText.length
}