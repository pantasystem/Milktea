package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.streaming.network.Socket

class TestSocketImpl : Socket{

    var state: Socket.State = Socket.State.NeverConnected

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


}