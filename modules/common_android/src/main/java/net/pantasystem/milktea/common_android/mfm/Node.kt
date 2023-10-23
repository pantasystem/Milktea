package net.pantasystem.milktea.common_android.mfm

open class Node(
    override val start: Int,
    override val end: Int,
    override val insideStart: Int,
    override val insideEnd: Int,
    override val elementType: ElementType

): Element {

    val childElements = ArrayList<Element>()
    override fun toString(): String {
        return "Node(type: $elementType, childNodes=$childElements)"
    }
}