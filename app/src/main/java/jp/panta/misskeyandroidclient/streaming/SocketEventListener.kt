package jp.panta.misskeyandroidclient.streaming


fun interface SocketStateEventListener {

    fun onStateChanged(e: Socket.State)
}

fun interface SocketMessageEventListener {

    fun onMessage(e: StreamingEvent): Boolean
}

