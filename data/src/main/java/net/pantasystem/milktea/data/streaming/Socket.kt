package net.pantasystem.milktea.data.streaming

import okhttp3.Response

/**
 * WebSocketを表すインターフェース
 */
interface Socket {

    sealed class State {
        /**
         * 接続済みを意味する
         */
        object Connected: State()

        /**
         * 現在接続を試みていることを表す
         */
        object Connecting: State()

        object Reconnecting : State()

        data class Closing(
            val code: Int,
            val reason: String
        ): State()

        object  NeverConnected : State()

        /**
         * 接続が失われていることを表す
         */
        data class Closed(
            val code: Int,
            val reason: String
        ): State()

        data class Failure(
            val throwable: Throwable,
            val response: Response?
        ) : State()
    }

    /**
     * 接続する
     * @return 状態が変化した場合true 現在の状態から何も変化しない場合はfalseが返されます
     */
    fun connect(): Boolean

    suspend fun blockingConnect(): Boolean

    /**
     * 切断する
     * @return 状態が変化した場合true 現在の状態から何も変化しない場合はfalseが返されます
     */
    fun disconnect(): Boolean

    fun reconnect()

    /**
     * 現在の状態を取得する
     */
    fun state(): State

    /**
     * メッセージを送信します。
     * @return Queueに追加された場合はtrueそうでない場合はfalseが返されます。
     */
    fun send(msg: String): Boolean

    fun onNetworkActive()
    fun onNetworkInActive()


    fun addStateEventListener(listener: SocketStateEventListener)

    fun removeStateEventListener(listener: SocketStateEventListener)

    fun addMessageEventListener(listener: SocketMessageEventListener)

    fun removeMessageEventListener(listener: SocketMessageEventListener)

}