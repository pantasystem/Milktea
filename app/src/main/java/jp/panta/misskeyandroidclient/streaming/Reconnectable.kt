package jp.panta.misskeyandroidclient.streaming

/**
 * 再接続時に再接続処理を行うインターフェース
 */
interface Reconnectable {

    fun onReconnect()
}