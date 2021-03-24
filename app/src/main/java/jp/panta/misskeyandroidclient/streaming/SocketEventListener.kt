package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.StreamingEvent



fun interface SocketStateEventListener {

    fun onStateChanged(e: Socket.State)
}

fun interface SocketMessageEventListener {

    fun onMessage(e: StreamingEvent): Boolean
}

fun interface BeforeConnectListener {
    /**
     * @return 接続処理を継続する場合は true そうでない場合はfalseを返します
     */
    fun onBeforeConnect(socket: Socket): Boolean
}