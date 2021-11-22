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
    loggerFactory: Logger.Factory,
) : Socket, WebSocketListener() {
    val logger = loggerFactory.create("SocketImpl")


    private var pollingJob: PollingJob = PollingJob(this)


    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var mWebSocket: WebSocket? = null
    private var mState: Socket.State = Socket.State.NeverConnected
        set(value) {
            field = value
            logger.debug("SocketImpl状態変化: ${value.javaClass}, $value")
            stateListeners.forEach {
                it.onStateChanged(value)
            }

        }

    private var stateListeners = setOf<SocketStateEventListener>()

    private var messageListeners = setOf<SocketMessageEventListener>()



    override fun addMessageEventListener(listener: SocketMessageEventListener) {

        val empty = messageListeners.isEmpty()
        messageListeners = messageListeners.toMutableSet().also {
            it.add(listener)
        }
        if(empty && messageListeners.isNotEmpty()) {
            connect()
        }

    }

    override fun addStateEventListener(listener: SocketStateEventListener) {
        stateListeners = stateListeners.toMutableSet().also {
            it.add(listener)
        }

    }

    override fun removeMessageEventListener(listener: SocketMessageEventListener) {
        messageListeners = messageListeners.toMutableSet().also {
            it.remove(listener)
        }
        if(messageListeners.isEmpty()) {
            disconnect()
        }
    }

    override fun removeStateEventListener(listener: SocketStateEventListener) {
        stateListeners = stateListeners.toMutableSet().also {
            it.remove(listener)
        }
    }

    override fun connect(): Boolean {
        synchronized(this){
            if(mWebSocket != null && onBeforeConnectListener.onBeforeConnect(this)){
                logger.debug("接続済みのためキャンセル")
                return false
            }
            if(!(mState is Socket.State.NeverConnected || mState is Socket.State.Failure || mState is Socket.State.Closed)) {
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
            var isResumed = false
            if(!connect()) {
                logger.debug("connect -> falseのためキャンセル")
                continuation.resume(false)
                isResumed = true
            }
            val callback = object : SocketStateEventListener{
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
            pollingJob.cancel()

            return mWebSocket?.close(1001, "finish")?: false
        }
    }

    override fun send(msg: String): Boolean {
        logger.debug("メッセージ送信: $msg, state${state()}")
        if(state() != Socket.State.Connected){
            logger.debug("送信をキャンセル state:${state()}")
            connect()
            return false
        }

        return mWebSocket?.send(msg)?: false
    }

    override fun state(): Socket.State {
        return this.mState

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
        runCatching {
            val r = json.decodeFromString<PongRes>(text)
            r.id.isNotBlank()
        }.onSuccess {
            logger.debug("polling成功")
            return
        }
        val e = runCatching { json.decodeFromString<StreamingEvent>(text) }.onFailure { t ->
            logger.error("デコードエラー msg:$text", e = t)
        }.getOrNull()?: return

        val iterator = messageListeners.iterator()
        while(iterator.hasNext()) {

            val listener = iterator.next()
            val res = runCatching {
                listener.onMessage(e)
            }.onFailure {
                logger.error("メッセージリスナー先でエラー発生", e = it)
            }.getOrElse {
                false
            }

            if(res){
                return
            }

        }

        logger.debug("受諾されんかったメッセージ: $text")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)


        logger.debug("onOpen webSocket 接続")
        synchronized(this){
            pollingJob.cancel()
            pollingJob = PollingJob(this).also {
                it.ping(4000, 900)
            }
            mState = Socket.State.Connected
        }
    }


}