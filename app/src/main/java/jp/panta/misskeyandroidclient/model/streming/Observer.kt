package jp.panta.misskeyandroidclient.model.streming

interface Observer {



    //val eventType: String

    /**
     * 接続時にStreamingAdapterから参照が与えられる
     */
    var streamingAdapter : StreamingAdapter?
    //fun onConnect(): StreamingAction
    //fun onDisconnect(): StreamingAction
    fun onConnect()
    fun onDisconnect()

    fun onReceived(msg: String)
}