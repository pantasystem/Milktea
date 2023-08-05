package jp.panta.misskeyandroidclient.streaming

import net.pantasystem.milktea.api_streaming.Socket
import net.pantasystem.milktea.api_streaming.SocketMessageEventListener
import net.pantasystem.milktea.api_streaming.SocketStateEventListener


class TestSocketImpl : Socket {

    private var state: Socket.State = Socket.State.NeverConnected
    private val messageListeners = mutableSetOf<SocketMessageEventListener>()
    private val stateEventListeners = mutableSetOf<SocketStateEventListener>()

    override fun connect(): Boolean {
        state = Socket.State.Connected
        return true

    }

    override fun disconnect(): Boolean {
        state = Socket.State.Closed(1001, "")
        return true
    }

    override fun send(msg: String, isAutoConnect: Boolean): Boolean {
        println("send: $msg")
        return true
    }

    override fun state(): Socket.State {
        return state

    }

    override fun addMessageEventListener(autoConnect: Boolean, listener: SocketMessageEventListener) {
        messageListeners.add(listener)
    }

    override fun addStateEventListener(listener: SocketStateEventListener) {
        stateEventListeners.add(listener)
    }

    override suspend fun blockingConnect(): Boolean {
        return connect()
    }

    override fun removeMessageEventListener(listener: SocketMessageEventListener) {
        messageListeners.remove(listener)
    }

    override fun removeStateEventListener(listener: SocketStateEventListener) {
        stateEventListeners.remove(listener)
    }


    override fun onNetworkActive() {
        throw Exception("未実装")
    }

    override fun onNetworkInActive() {
        throw Exception("未実装")
    }

    override fun reconnect() {
        throw Exception("未実装")
    }

}