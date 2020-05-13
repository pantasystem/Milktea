package jp.panta.misskeyandroidclient.mfm

import java.io.Serializable

interface Element : Serializable{
    val start: Int
    val end: Int
    val insideStart: Int
    val insideEnd: Int
    val elementType: ElementType
}