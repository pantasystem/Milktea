package jp.panta.misskeyandroidclient.streaming

class TestSocketImpl : Socket {

    var state: Socket.State = Socket.State.NeverConnected
    val listeners = mutableListOf<SocketEventListener>()

    override fun connect(): Boolean {
        state = Socket.State.Connected
        return true

    }

    override fun disconnect(): Boolean {
        state = Socket.State.Closed(1001, "")
        return true
    }

    override fun send(msg: String): Boolean {
        println("send: $msg")
        return true
    }

    override fun state(): Socket.State {
        return state

    }

    override fun addSocketEventListener(listener: SocketEventListener) {
        listeners.add(listener)
    }




}