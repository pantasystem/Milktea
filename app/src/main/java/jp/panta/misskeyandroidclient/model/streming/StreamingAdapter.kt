package jp.panta.misskeyandroidclient.model.streming

import android.os.Handler
import android.util.Log
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import okhttp3.*
import okio.ByteString
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * StreamingAdapter、１につきアカウント１にしたい
 */
class StreamingAdapter(
    private val connectionInformation: EncryptedConnectionInformation?,
    private val encryption: Encryption
){

    companion object{
        val instanceCount = AtomicInteger()
    }
    init{
        val count = instanceCount.incrementAndGet()
        Log.d("StreamingAdapter", "生成数:$count")

    }

    private var mWebSocket: WebSocket? = null

    private val TAG = "StreamingAdapter"

    //val observers = ArrayList<Observer>()
    val observerMap = WeakHashMap<String, Observer>()

    var isConnect: Boolean = false
        private set




    fun putObserver(observer: Observer){
        synchronized(observerMap){
            if(observerMap.isEmpty()){
                connect()
            }

            if(isConnect){
                observer.streamingAdapter = this
                val exObserver = observerMap[observer.id]
                if(exObserver != null){
                    Log.d("StreamingAdapter", "追加済みのObserverを再追加しようとしたため古い接続は閉じられました。Hint:IDの重複")
                    exObserver.onDisconnect()
                }

                observerMap[observer.id] = observer
                observer.onConnect()
            }else{
                Handler().postDelayed({
                    putObserver(observer)
                }, 100)
            }
        }

    }

    fun removeObserver(observer: Observer){
        synchronized(observerMap){
            observer.onDisconnect()
            val ex = observerMap[observer.id]
            if(ex == null){
                Log.d("StreamingAdapter", "追加されていないObserverを削除しようとしました")
            }else{
                val removed = observerMap.remove(ex.id)
                removed?.onClosing()
                removed?.streamingAdapter = null
            }
            if(observerMap.isEmpty()){
                this.disconnect()
                Log.d("StreamingAdapter", "Observerが0件になったためWebSocketを切断しました")
            }
        }

    }


    fun send(json: String){
        mWebSocket?.send(json)
    }

    private fun connect(){
        if(mWebSocket == null){
            val wssUrl = connectionInformation?.instanceBaseUrl?.replace("https://", "wss://") + "/streaming?i=${connectionInformation?.getI(encryption)}"
            val request = Request.Builder()
                .url(wssUrl)
                .build()
            mWebSocket = OkHttpClient().newWebSocket(request, webSocketListener)
        }

    }

    fun disconnect(){
        synchronized(observerMap){
            observerMap.values.forEach{ observer ->
                observer.onClosing()
            }
        }
        mWebSocket?.close(1001, "close")
    }

    private val webSocketListener = object : WebSocketListener(){
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "onOpenコネクション開始")
            isConnect = true
            synchronized(observerMap){
                observerMap.forEach {
                    it.value.onConnect()
                }
            }

        }

        override fun onMessage(webSocket: WebSocket, text: String) {

            //Log.d(TAG, "onMessage: $text")

            if (text.isNotBlank()) {
                synchronized(observerMap){
                    observerMap.forEach{
                        try{
                            it.value.onReceived(text)
                        }catch(e: Exception){
                            Log.d(TAG, "error", e)
                        }
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
            mWebSocket = null

        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosing: 通信を閉じている code: $code")
            isConnect = false

        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d(TAG, "onFailure: ERROR通信が途絶えてしまった", t)
            isConnect = false
            synchronized(observerMap){
                observerMap.forEach {
                    it.value.onDisconnect()
                }
            }

            Thread.sleep(2000)
            mWebSocket = null
            this@StreamingAdapter.connect()

        }
    }

}