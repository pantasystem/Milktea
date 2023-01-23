package net.pantasystem.milktea.api_streaming.network

import net.pantasystem.milktea.api_streaming.Socket
import net.pantasystem.milktea.api_streaming.SocketMessageEventListener
import net.pantasystem.milktea.api_streaming.SocketStateEventListener

/**
 * 実装の都合上MastodonなのにMisskeyのStreaming APIに無理やり接続しようとするため
 * 404になってかつ何度もリトライしてしまいDoS攻撃みたいなことになっているので空の実装を作成
 */
class MastodonSocketImpl : Socket {
    override fun connect(): Boolean {
        return true
    }

    override suspend fun blockingConnect(): Boolean {
        return true
    }

    override fun disconnect(): Boolean {
        return true
    }

    override fun reconnect() {
    }

    override fun state(): Socket.State {
        return Socket.State.Closed(1000, "")
    }

    override fun send(msg: String, isAutoConnect: Boolean): Boolean {
        return true
    }

    override fun onNetworkActive() {
    }

    override fun onNetworkInActive() {
    }

    override fun addStateEventListener(listener: SocketStateEventListener) {
    }

    override fun removeStateEventListener(listener: SocketStateEventListener) {
    }

    override fun addMessageEventListener(listener: SocketMessageEventListener) {
    }

    override fun removeMessageEventListener(listener: SocketMessageEventListener) {
    }
}