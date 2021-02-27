package jp.panta.misskeyandroidclient.streaming.network

import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.SocketEventListener
import jp.panta.misskeyandroidclient.streaming.StreamingEvent
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.*

class SocketImpl(
    val url: String,
    val okHttpClient: OkHttpClient = OkHttpClient()
) : Socket, WebSocketListener() {


    val json = Json {
        ignoreUnknownKeys = true
    }

    private var mWebSocket: WebSocket? = null
    private var mState: Socket.State = Socket.State.NeverConnected
        set(value) {
            field = value
            listeners.forEach {
                it.onStateChanged(value)
            }
        }

    private val listeners = mutableSetOf<SocketEventListener>()

    override fun addSocketEventListener(listener: SocketEventListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override fun connect(): Boolean {
        synchronized(this){
            if(mWebSocket != null){
                return false
            }
            mState = Socket.State.Connecting
            val request = Request.Builder()
                .url(url)
                .build()

            mWebSocket = okHttpClient.newWebSocket(request, this)
            return mWebSocket != null
        }

    }

    override fun disconnect(): Boolean {
        synchronized(this){
            if(mWebSocket == null){
                return false
            }

            return mWebSocket?.close(1001, "finish")?: false
        }
    }

    override fun send(msg: String): Boolean {
        synchronized(this){
            if(state() != Socket.State.Connected){
                return false
            }

            return mWebSocket?.send(msg)?: false
        }
    }

    override fun state(): Socket.State {
        synchronized(this){
            return this.mState
        }

    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)

        synchronized(this){
            mState = Socket.State.Closed(code, reason)
        }
    }


    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)

        synchronized(this){
            mState = Socket.State.Closing(code, reason)
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)

        synchronized(this) {
            mState = Socket.State.Failure(
                t, response
            )
        }
    }



    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        synchronized(listeners) {
            for(listener in listeners) {
                if(listener.onMessage(json.decodeFromString(text))) {
                    break
                }
            }
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        synchronized(this){
            mState = Socket.State.Connected
        }
    }
}