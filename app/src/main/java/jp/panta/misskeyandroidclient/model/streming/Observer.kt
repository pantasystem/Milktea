package jp.panta.misskeyandroidclient.model.streming

import jp.panta.misskeyandroidclient.model.core.Account
import java.util.*

interface Observer {



    //val eventType: String

    /**
     * 接続時にStreamingAdapterから参照が与えられる
     */
    var streamingAdapter : StreamingAdapter?
    //fun onConnect(): StreamingAction
    //fun onDisconnect(): StreamingAction
    val id: String

    val account: Account

    /**
     * 接続が開始されたときに呼び出されます
     * 主にサーバーに対してのリソース登録のメッセージを送信したりします。
     */
    fun onConnect()

    /**
     * サーバーに対してのリソース解放を行う
     * 例えばノートのキャプチャーならノートのキャプチャーを解除するなど
     */
    fun onClosing()

    /**
     * すでにWebSocketは切断されています
     * アプリ内のリソースを解放します
     */
    fun onDisconnect()

    fun onReceived(msg: String)
}

abstract class AbsObserver : Observer{
    override val id: String = UUID.randomUUID().toString()
}