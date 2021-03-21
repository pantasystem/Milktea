package jp.panta.misskeyandroidclient.streaming.network

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.streaming.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SocketImpl(
    val url: String,
    val okHttpClient: OkHttpClient = OkHttpClient(),
    val onBeforeConnectListener: BeforeConnectListener,
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
            synchronized(stateListeners) {
                stateListeners.toList().forEach {
                    it.onStateChanged(value)
                }
            }

        }

    private val stateListeners = mutableSetOf<SocketStateEventListener>()

    private val messageListeners = mutableSetOf<SocketMessageEventListener>()



    override fun addMessageEventListener(listener: SocketMessageEventListener) {
        synchronized(this) {
            val empty = messageListeners.isEmpty()
            messageListeners.add(listener)
            if(empty && messageListeners.isNotEmpty()) {
                connect()
            }
        }
    }

    override fun addStateEventListener(listener: SocketStateEventListener) {
        synchronized(this) {
            stateListeners.add(listener)
        }
    }

    override fun removeMessageEventListener(listener: SocketMessageEventListener) {
        synchronized(this) {
            messageListeners.remove(listener)
            if(messageListeners.isEmpty()) {
                disconnect()
            }
        }
    }

    override fun removeStateEventListener(listener: SocketStateEventListener) {
        synchronized(this) {
            stateListeners.remove(listener)
        }
    }

    override fun connect(): Boolean {
        synchronized(this){
            if(mWebSocket != null && onBeforeConnectListener.onBeforeConnect(this)){
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
            val callback = object : SocketStateEventListener{
                var isResumed = false
                override fun onStateChanged(e: Socket.State) {
                    if(!isResumed) {
                        if(e is Socket.State.Connected){
                            removeStateEventListener(this)
                            continuation.resume(true)
                        }else if(e is Socket.State.Failure || e is Socket.State.Closed) {
                            removeStateEventListener(this)
                            continuation.resume(false)
                        }
                        isResumed = true
                    }

                }

            }
            addStateEventListener(callback)
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
        logger.debug("メッセージ送信: $msg, state${state()}")
        synchronized(this){
            if(state() != Socket.State.Connected){
                logger.debug("送信をキャンセル state:${state()}")
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
        logger.error("onFailure, res=$response", e = t)

        synchronized(this) {
            mState = Socket.State.Failure(
                t, response
            )
            mWebSocket = null
        }
    }



    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)

        synchronized(this) {
            val iterator = messageListeners.iterator()
            while(iterator.hasNext()) {
                val e = runCatching { json.decodeFromString<StreamingEvent>(text) }.getOrNull()
                    ?: continue
                val listener = iterator.next()
                if(listener.onMessage(e)){
                    break
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