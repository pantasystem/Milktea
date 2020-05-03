package jp.panta.misskeyandroidclient.model.streming

import android.util.Log
import com.bumptech.glide.RequestBuilder
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import okhttp3.*
import okio.ByteString
import java.util.*

/**
 * StreamingAdapter、１につきアカウント１にしたい
 */
class StreamingAdapter(
    private val connectionInformation: EncryptedConnectionInformation?,
    private val encryption: Encryption
){


    private var mWebSocket: WebSocket? = null

    private val TAG = "StreamingAdapter"

    //val observers = ArrayList<Observer>()
    val observerMap = WeakHashMap<String, Observer>()

    var isConnect: Boolean = false
        private set


    fun addObserver(id: String, observer: Observer){
        //observer.onConnect()
        observer.streamingAdapter = this
        //observers.add(observer)
        val exObserver = observerMap[id]
        if(exObserver != null){
            Log.d(TAG, "既存のObserverを検出したので切断しました")
        }
        exObserver?.onDissconnect()

        observerMap[id] = observer
    }


    fun send(json: String){
        mWebSocket?.send(json)
    }

    fun connect(){
        val wssUrl = connectionInformation?.instanceBaseUrl?.replace("https://", "wss://") + "/streaming?i=${connectionInformation?.getI(encryption)}"
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
            observerMap.forEach {
                it.value.onConnect()
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {

            //Log.d(TAG, "onMessage: $text")

            if (text.isNotBlank()) {
                observerMap.forEach{
                    try{
                        it.value.onReceived(text)
                    }catch(e: Exception){
                        Log.d(TAG, "error", e)
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
            observerMap.forEach {
                it.value.onDissconnect()
            }
            Thread.sleep(2000)
            this@StreamingAdapter.connect()

        }
    }

}