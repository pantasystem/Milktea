package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.StreamingEvent



fun interface SocketStateEventListener {

    fun onStateChanged(e: Socket.State)
}

fun interface SocketMessageEventListener {

    fun onMessage(e: StreamingEvent): Boolean
}

