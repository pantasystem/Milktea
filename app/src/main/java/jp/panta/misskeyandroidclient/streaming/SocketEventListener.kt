package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.StreamingEvent

/**
 * Streamingのメッセージを処理するHandler
 */
interface SocketEventListener {

    /**
     * @return これ以降のメッセージの処理を中断する場合は true, 続行する場合はfalseを返します
     */
    fun onMessage(e: StreamingEvent): Boolean

    fun onStateChanged(e: Socket.State)
}