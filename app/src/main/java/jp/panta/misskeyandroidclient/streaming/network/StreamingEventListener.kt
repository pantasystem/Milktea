package jp.panta.misskeyandroidclient.streaming.network

import jp.panta.misskeyandroidclient.streaming.StreamingEvent

/**
 * Streamingのメッセージを処理するHandler
 */
interface StreamingEventListener {

    /**
     * @return これ以降のメッセージの処理を中断する場合は true, 続行する場合はfalseを返します
     */
    fun handle(e: StreamingEvent): Boolean
}