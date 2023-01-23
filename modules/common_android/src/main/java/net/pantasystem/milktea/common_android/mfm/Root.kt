package jp.panta.misskeyandroidclient.mfm

import net.pantasystem.milktea.common_android.mfm.ElementType

class Root(
    val sourceText: String
) : Node(0, sourceText.length, 0, sourceText.length, ElementType.ROOT){
    override val elementType: ElementType = ElementType.ROOT
    override val start: Int = 0
    override val end: Int = sourceText.length
    override val insideStart: Int = 0
    override val insideEnd: Int = sourceText.length

    fun getUrls(urls: LinkedHashSet<String> = LinkedHashSet(), node: Node = this): List<String>{

        node.childElements.forEach { el ->
            if(el is Node){
                getUrls(urls, el)
            }else if(el is Link){
                urls.add(el.url)
            }
        }
        return urls.toList()
    }
}