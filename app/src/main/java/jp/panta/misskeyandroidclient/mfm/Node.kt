package jp.panta.misskeyandroidclient.mfm

open class Node(
    val start: Int,
    val end: Int,
    val insideStart: Int,
    val insideEnd: Int,
    val tag: TagType,
    val parentNode: Node?

): Element {

    val childNodes = ArrayList<Element>()
}