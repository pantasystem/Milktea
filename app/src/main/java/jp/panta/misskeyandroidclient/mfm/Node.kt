package jp.panta.misskeyandroidclient.mfm

class Node(
    val startTag: Tag,
    val endTag: Tag,
    val parentNode: Node?

): Element {
    /**
     * タグを除いた内側の文字列インデックス
     */
    val contentStart = startTag.end
    val contentEnd = endTag.start

    val childNodes = ArrayList<Node>()
}