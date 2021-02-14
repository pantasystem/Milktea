package jp.panta.misskeyandroidclient.streaming.network

/**
 * Streamingのメッセージを処理するHandler
 */
interface MessageReceiveListener {

    /**
     * @return これ以降のメッセージの処理を中断する場合は true, 続行する場合はfalseを返します
     */
    fun onReceiveMessage(message: String) : Boolean
}