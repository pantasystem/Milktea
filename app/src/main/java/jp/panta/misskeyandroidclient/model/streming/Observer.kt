package jp.panta.misskeyandroidclient.model.streming

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
    fun onConnect()
    fun onDisconnect()

    fun onReceived(msg: String)
}

abstract class AbsObserver : Observer{
    override val id: String = UUID.randomUUID().toString()
}