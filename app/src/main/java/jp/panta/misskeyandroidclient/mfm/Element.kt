package jp.panta.misskeyandroidclient.mfm

interface Element{
    val start: Int
    val end: Int
    val insideStart: Int
    val insideEnd: Int
    val elementType: ElementType
}