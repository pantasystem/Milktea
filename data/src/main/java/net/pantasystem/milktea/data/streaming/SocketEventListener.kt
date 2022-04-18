package net.pantasystem.milktea.data.streaming


fun interface SocketStateEventListener {

    fun onStateChanged(e: Socket.State)
}

fun interface SocketMessageEventListener {

    fun onMessage(e: StreamingEvent): Boolean
}

