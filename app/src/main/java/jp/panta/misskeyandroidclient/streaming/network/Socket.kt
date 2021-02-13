package jp.panta.misskeyandroidclient.streaming.network

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

        object Closing: State()

        /**
         * 接続が失われていることを表す
         */
        object Closed: State()
    }

    /**
     * 接続する
     * @return 状態が変化した場合true 現在の状態から何も変化しない場合はfalseが返されます
     */
    fun connect(): Boolean

    /**
     * 切断する
     * @return 状態が変化した場合true 現在の状態から何も変化しない場合はfalseが返されます
     */
    fun disconnect(): Boolean

    /**
     * 現在の状態を取得する
     */
    fun state(): State

    /**
     * メッセージを送信します。
     * @return Queueに追加された場合はtrueそうでない場合はfalseが返されます。
     */
    fun send(msg: String): Boolean
}