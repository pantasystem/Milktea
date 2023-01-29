package net.pantasystem.milktea.api_streaming.network

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.api_streaming.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SocketImpl(
    val url: String,
    loggerFactory: Logger.Factory,
    okHttpClientProvider: OkHttpClientProvider
    ) : Socket {
    val logger = loggerFactory.create("SocketImpl")

    private val okHttpClient: OkHttpClient = okHttpClientProvider
        .get()
        .newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.HOURS)
        .readTimeout(1, TimeUnit.HOURS)
        .build()

    private var pollingJob: PollingJob = PollingJob(this)


    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var mWebSocket: WebSocket? = null
    private var mState: Socket.State = Socket.State.NeverConnected
        set(value) {
            field = value
            logger.debug { "SocketImpl状態変化: ${value.javaClass.simpleName}, $value}" }
            stateListeners.forEach {
                it.onStateChanged(value)
            }

        }

    private var stateListeners = setOf<SocketStateEventListener>()

    private var messageListeners = setOf<SocketMessageEventListener>()
    private var isNetworkActive = true

    /**
     * 等インスタンスを使用して接続を行う可能性が永続的にない場合はtrueになる。
     * 主にログアウト処理が行われた際や再認証処理が行われ、インスタンスが使われなくなる可能性がある場合もtrueになる。
     */
    private var isDestroyed: Boolean = false


    override fun addMessageEventListener(listener: SocketMessageEventListener) {

        val empty = messageListeners.isEmpty()
        messageListeners = messageListeners.toMutableSet().also {
            it.add(listener)
        }
        if (empty && messageListeners.isNotEmpty()) {
            try {
                connect()
            } catch (e: Exception) {
                logger.error("接続失敗", e)
            }
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
        if (messageListeners.isEmpty()) {
            disconnect()
        }
    }

    override fun removeStateEventListener(listener: SocketStateEventListener) {
        stateListeners = stateListeners.toMutableSet().also {
            it.remove(listener)
        }
    }

    override fun connect(): Boolean {
        synchronized(this) {
            if (mWebSocket != null) {
                logger.debug { "接続済みのためキャンセル" }
                return false
            }
            if (!isNetworkActive) {
                logger.debug { "ネットワークがアクティブではないのでキャンセル" }
                return false
            }

            if (isDestroyed) {
                logger.debug { "destroyedされているのでキャンセル" }
                return false
            }

            if (!(mState is Socket.State.NeverConnected || mState is Socket.State.Failure || mState is Socket.State.Closed)) {
                return false
            }
            mState = Socket.State.Connecting(false)
            val request = Request.Builder()
                .url(url)
                .build()

            mWebSocket = okHttpClient.newWebSocket(request, WebSocketListenerImpl())
            return true
        }

    }

    override fun reconnect() {
        synchronized(this) {
            mWebSocket?.cancel()
            mWebSocket = null
        }
        mState = Socket.State.Connecting(true)


    }

    override suspend fun blockingConnect(): Boolean {
        return suspendCoroutine { continuation ->
            var isResumed = false
            if (!connect()) {
                logger.debug { "connect -> falseのためキャンセル" }
                continuation.resume(false)
                isResumed = true
            }
            val callback = object : SocketStateEventListener {
                override fun onStateChanged(e: Socket.State) {
                    if (!isResumed) {
                        if (e is Socket.State.Connected) {
                            removeStateEventListener(this)
                            continuation.resume(true)
                        } else if (e is Socket.State.Failure || e is Socket.State.Closed) {
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
        synchronized(this) {
            if (mWebSocket == null) {
                return false
            }
            pollingJob.cancel()

            val result = mWebSocket?.close(1001, "finish") ?: false
            mWebSocket = null
            return result
        }
    }

    override fun send(msg: String, isAutoConnect: Boolean): Boolean {
        logger.debug { "メッセージ送信: $msg, state${state()}" }
        if (state() != Socket.State.Connected) {
            logger.debug { "送信をキャンセル state:${state()}" }
            if (isAutoConnect) {
                connect()
            }
            return false
        }

        synchronized(this) {
            return mWebSocket?.send(msg) ?: false
        }
    }

    override fun state(): Socket.State {
        return this.mState

    }


    override fun onNetworkActive() {
        isNetworkActive = true
        connect()
    }

    override fun onNetworkInActive() {
        isNetworkActive = false
    }

    /**
     * ログアウトや再認証によってこのインスタンスを用いて接続処理などを行わない可能性がある場合がある時に呼び出される。
     */
    fun destroy(): Boolean {
        synchronized(this) {
            messageListeners = emptySet()
            isDestroyed = true
        }
        return disconnect()
    }


    inner class WebSocketListenerImpl : WebSocketListener() {
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            logger.debug { "WebSocketをClose: $code" }

            synchronized(this@SocketImpl) {
                mState = Socket.State.Closed(code, reason)
                pollingJob.cancel()
                mWebSocket = null
                webSocket.cancel()
            }
        }


        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            synchronized(this@SocketImpl) {
                pollingJob.cancel()
                mState = Socket.State.Closing(code, reason)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            logger.error("onFailure", e = t)


            val state = mState
            synchronized(this@SocketImpl) {
                pollingJob.cancel()
                mWebSocket = null

                webSocket.cancel()
                mState = Socket.State.Failure(
                    t, response?.code, response?.message,
                )
            }

            // NOTE: 再接続以外の時はレートリミットを入れる
            if (!(state is Socket.State.Connecting && state.isReconnect)) {
                Thread.sleep(2000)
            }
            connect()
        }


        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            runCancellableCatching {
                pollingJob.onReceive(text)
            }.onSuccess {
                return
            }
            val e = runCancellableCatching { json.decodeFromString<StreamingEvent>(text) }.onFailure { t ->
                logger.error("デコードエラー msg:$text", e = t)
            }.getOrNull() ?: return

            val iterator = messageListeners.iterator()
            while (iterator.hasNext()) {

                val listener = iterator.next()
                val res = runCancellableCatching {
                    listener.onMessage(e)
                }.onFailure {
                    logger.error("メッセージリスナー先でエラー発生", e = it)
                }.getOrElse {
                    false
                }

                if (res) {
                    return
                }

            }

            logger.debug { "受諾されんかったメッセージ: $text" }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)


            logger.debug { "onOpen webSocket 接続" }
            synchronized(this@SocketImpl) {
                pollingJob.cancel()
                pollingJob = PollingJob(this@SocketImpl).also {
                    it.startPolling(4000, 900, 12000)
                }
                mState = Socket.State.Connected
            }
        }
    }
}
