package jp.panta.misskeyandroidclient.streaming.network

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.SocketEventListener
import jp.panta.misskeyandroidclient.streaming.StreamingEvent
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.*
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SocketImpl(
    val url: String,
    val okHttpClient: OkHttpClient = OkHttpClient(),
    loggerFactory: Logger.Factory
) : Socket, WebSocketListener() {
    val logger = loggerFactory.create("SocketImpl")


    val json = Json {
        ignoreUnknownKeys = true
    }

    private var mWebSocket: WebSocket? = null
    private var mState: Socket.State = Socket.State.NeverConnected
        set(value) {
            field = value
            logger.debug("SocketImpl状態変化: ${value.javaClass}, $value")
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

    override fun removeSocketEventListener(listener: SocketEventListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    override fun connect(): Boolean {
        synchronized(this){
            if(mWebSocket != null){
                logger.debug("接続済みのためキャンセル")
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

    override suspend fun blockingConnect(): Boolean {
        return suspendCoroutine { continuation ->
            if(!connect()) {
                logger.debug("connect -> falseのためキャンセル")
                continuation.resume(false)
            }
            val callback = object : SocketEventListener {
                override fun onMessage(e: StreamingEvent): Boolean = false
                override fun onStateChanged(e: Socket.State) {
                    if(e is Socket.State.Connected){
                        removeSocketEventListener(this)
                        continuation.resume(true)
                    }else if(e is Socket.State.Failure || e is Socket.State.Closed) {
                        removeSocketEventListener(this)
                        continuation.resume(false)
                    }
                }
            }
            addSocketEventListener(callback)
            return@suspendCoroutine
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
        logger.debug("メッセージ送信: $msg")
        synchronized(this){
            if(state() != Socket.State.Connected){
                logger.debug("送信をキャンセル")
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
        logger.debug("WebSocketをClose: $code")

        synchronized(this){
            mState = Socket.State.Closed(code, reason)
            mWebSocket = null
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
            mWebSocket = null
        }
    }



    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        synchronized(listeners) {
            for(listener in listeners) {
                try {
                    if(listener.onMessage(json.decodeFromString(text))){
                        break
                    }
                }catch (e: Exception) {
                    logger.error("onMessage: error msg: $text", e = e)
                }
            }
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        logger.debug("webSocket:url=$url 接続")
        synchronized(this){
            mState = Socket.State.Connected
        }
    }
}