package jp.panta.misskeyandroidclient.model.streming

import android.util.Log
import com.bumptech.glide.RequestBuilder
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import okhttp3.*
import okio.ByteString

class StreamingAdapter(
    private val connectionInstance: ConnectionInstance
){


    private var mWebSocket: WebSocket? = null

    private val TAG = "StreamingAdapter"

    val observers = ArrayList<Observer>()

    var isConnect: Boolean = false
        private set


    fun addObserver(observer: Observer){
        //observer.onConnect()
        observer.streamingAdapter = this
        observers.add(observer)
    }


    fun send(json: String){
        mWebSocket?.send(json)
    }

    fun connect(){
        val wssUrl = connectionInstance.instanceBaseUrl.replace("https://", "wss://") + "/streaming?i=${connectionInstance.getI()}"
        val request = Request.Builder()
            .url(wssUrl)
            .build()
        mWebSocket = OkHttpClient().newWebSocket(request, webSocketListener)
    }

    fun disconnect(){
        mWebSocket?.close(1001, "close")
    }

    private val webSocketListener = object : WebSocketListener(){
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "onOpenコネクション開始")
            isConnect = true
            observers.forEach{
                it.onConnect()
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {

            //Log.d(TAG, "onMessage: $text")

            if (text.isNotBlank()) {
                observers.forEach {
                    try{
                        it.onReceived(text)
                    }catch(e: Exception){
                        Log.d("StreamingAdapter", "error")
                    }
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {

            Log.d(TAG, "onMessage, bytes $bytes")


        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClose: 通信が途絶えてしまった code: $code")
            //mWebSocket = null
            isConnect = false

        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosing: 通信を閉じている code: $code")
            isConnect = false

        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d(TAG, "onFailure: ERROR通信が途絶えてしまった", t)
            isConnect = false

            observers.forEach {
                it.onDissconnect()
            }
            Thread.sleep(1000)
            connect()

        }
    }

}