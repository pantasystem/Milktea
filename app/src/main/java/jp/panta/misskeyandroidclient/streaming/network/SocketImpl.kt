package jp.panta.misskeyandroidclient.streaming.network

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.streaming.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SocketImpl(
    val url: String,
    val okHttpClient: OkHttpClient = OkHttpClient(),
    loggerFactory: Logger.Factory,
) : Socket, WebSocketListener() {
    val logger = loggerFactory.create("SocketImpl")


    private var pollingJob: PollingJob = PollingJob(this)

    private val lock = Mutex()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var mWebSocket: WebSocket? = null
    private var mState: Socket.State = Socket.State.NeverConnected
        set(value) {
            field = value
            logger.debug("SocketImpl状態変化: ${value.javaClass.simpleName}, $value}")
            stateListeners.forEach {
                it.onStateChanged(value)
            }

        }

    private var stateListeners = setOf<SocketStateEventListener>()

    private var messageListeners = setOf<SocketMessageEventListener>()
    private var isNetworkActive = true



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
        return lock.blockingWithLockWithCheckTimeout {
            if(mWebSocket != null && isNetworkActive){
                logger.debug("接続済みのためキャンセル")
                return@blockingWithLockWithCheckTimeout false
            }
            if(!(mState is Socket.State.NeverConnected || mState is Socket.State.Failure || mState is Socket.State.Closed)) {
                return@blockingWithLockWithCheckTimeout false
            }
            mState = Socket.State.Connecting
            val request = Request.Builder()
                .url(url)
                .build()

            mWebSocket = okHttpClient.newWebSocket(request, this)
            return@blockingWithLockWithCheckTimeout mWebSocket != null
        }

    }

    override fun reconnect() {
        val ws = mWebSocket
        lock.blockingWithLockWithCheckTimeout {
            mWebSocket = null
        }
        mState = Socket.State.Reconnecting
        ws?.cancel()
        pollingJob.cancel()
        this.connect()

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
        return lock.blockingWithLockWithCheckTimeout  {
            if(mWebSocket == null){
                return@blockingWithLockWithCheckTimeout false
            }
            pollingJob.cancel()

            val result = mWebSocket?.close(1001, "finish")?: false
            mWebSocket = null
            return@blockingWithLockWithCheckTimeout result
        }
    }

    override fun send(msg: String): Boolean {
        logger.debug("メッセージ送信: $msg, state${state()}")
        if(state() != Socket.State.Connected){
            logger.debug("送信をキャンセル state:${state()}")
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

        lock.blockingWithLockWithCheckTimeout{
            mState = Socket.State.Closed(code, reason)
            pollingJob.cancel()
            mWebSocket = null
        }
    }


    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)

        lock.blockingWithLockWithCheckTimeout {
            pollingJob.cancel()
            mState = Socket.State.Closing(code, reason)
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        logger.error("onFailure, res=$response", e = t)


        val state = mState
        lock.blockingWithLockWithCheckTimeout {
            pollingJob.cancel()

            mWebSocket = null
            mState = Socket.State.Failure(
                t, response
            )
        }

        // NOTE: 再接続以外の時はレートリミットを入れる
        if(state !is Socket.State.Reconnecting) {
            Thread.sleep(2000)
        }
        connect()
    }



    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        runCatching {
            val r = json.decodeFromString<PongRes>(text)
            r.id.isNotBlank()
            pollingJob.onReceive(r)
        }.onSuccess {
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
        lock.blockingWithLockWithCheckTimeout {
            pollingJob.cancel()
            pollingJob = PollingJob(this).also {
                it.ping(4000, 900, 12000)
            }
            mState = Socket.State.Connected
        }
    }

    override fun onNetworkActive() {
        isNetworkActive = true
        connect()
    }

    override fun onNetworkInActive() {
        isNetworkActive = false
    }

}

fun<T> Mutex.blockingWithLockWithCheckTimeout(owner: Any? = null, action: () -> T): T {
    return runBlocking {
        withTimeout(1000) {
            withLock(owner = owner, action = action)
        }
    }
}