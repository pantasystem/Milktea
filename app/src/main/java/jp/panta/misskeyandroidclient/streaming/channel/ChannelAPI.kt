package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.streaming.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class ChannelAPI(
    val socket: Socket,
    loggerFactory: Logger.Factory,
) : SocketMessageEventListener, SocketStateEventListener {

    enum class Type {
        MAIN, HOME, LOCAL, HYBRID, GLOBAL
    }

    private val logger = loggerFactory.create("ChannelAPI")

    private var listenersMap = mapOf<Type, Set<(ChannelBody)->Unit>>(
        Type.MAIN to hashSetOf(),
        Type.HOME to hashSetOf(),
        Type.LOCAL to hashSetOf(),
        Type.HYBRID to hashSetOf(),
        Type.GLOBAL to hashSetOf()
    )

    private var typeIdMap = mapOf<Type, String>()
    private val mutex = Mutex()

    init {
        //socket.addMessageEventListener(this)
        socket.addStateEventListener(this)
    }

    @ExperimentalCoroutinesApi
    fun connect(type: Type) : Flow<ChannelBody> {
        return channelFlow {
            val callback: (ChannelBody)-> Unit = {
                val result = trySend(it)
                if(!result.isSuccess){
                    logger.debug("スルーされたメッセージ: $it")
                }
                //logger.debug("ChannelAPI message:${if(it.toString().length > 50) it.toString().subSequence(0, 50) else it.toString()}")
            }
            connect(type, callback)

            awaitClose {
                disconnect(type, callback)

            }
        }
    }

    /**
     * 接続しているチャンネル数
     */
    fun count(): Int = typeIdMap.count()

    /**
     * 接続しているチャンネル数がゼロであるか
     */
    fun isEmpty(): Boolean {
        return count() == 0
    }

    private fun connect(type: Type, listener: (ChannelBody)->Unit): Unit = runBlocking{
        // NOTE すでにlistenerを追加済みであれば何もせずに終了する。
        mutex.withLock {
            if(listenersMap[type]?.contains(listener) == true) {
                logger.debug("リッスン済み")
                return@runBlocking
            }

            val sets = listenersMap[type]?.toMutableSet()?.also {
                it.add(listener)
            } ?: throw IllegalStateException("listenersがNULLです。")
            listenersMap = listenersMap.toMutableMap().also {
                it[type] = sets
            }

            if(typeIdMap.isEmpty()) {
                socket.addMessageEventListener(this@ChannelAPI)
            }
            if(typeIdMap[type] == null){
                logger.debug("接続処理を開始")
                sendConnect(type)
                logger.debug("after sendConnect:${typeIdMap}")
            }
        }

    }

    private fun disconnect(type: Type, listener: (ChannelBody) -> Unit) = runBlocking{
        mutex.withLock {
            if(listenersMap[type]?.contains(listener) != true){
                return@runBlocking
            }

            listenersMap = listenersMap.toMutableMap().also {
                it[type] = (it[type]?.toMutableSet()?: emptySet()).toMutableSet().also { set ->
                    set.remove(listener)
                }
            }

            // 誰にも使われていなければサーバーからChannelへの接続を開放する
            trySendDisconnect(type)
            if(typeIdMap.isEmpty()) {
                socket.removeMessageEventListener(this@ChannelAPI)
            }
        }

    }


    override fun onMessage(e: StreamingEvent): Boolean {
        if(e is ChannelEvent) {
            val st = e.toString()
            //logger.debug("ChannelEvent: ${if(st.length > 100) st.substring(0,100) else st}..., callbacks main:${listenersMap[Type.MAIN]?.size}, global: ${listenersMap[Type.GLOBAL]?.size}, hybrid: ${listenersMap[Type.GLOBAL]?.size}, home:${listenersMap[Type.HOME]?.size}, type=${typeIdMap.filter { it.value == e.body.id }}")

            typeIdMap.filter {
                it.value == e.body.id
            }.keys.forEach {
                listenersMap[it]?.forEach { callback ->
                    callback.invoke(e.body)
                }?: throw IllegalStateException("未実装なTypeです。")
            }

            return true
        }

        return false
    }

    /**
     * 接続メッセージを現在の状態にかかわらずサーバーに送信する
     */
    private fun sendConnect(type: Type): Boolean {
        logger.debug("sendConnect($type)")
        val body = when(type){
            Type.GLOBAL -> Send.Connect.Type.GLOBAL_TIMELINE
            Type.HYBRID -> Send.Connect.Type.HYBRID_TIMELINE
            Type.LOCAL -> Send.Connect.Type.LOCAL_TIMELINE
            Type.HOME -> Send.Connect.Type.HOME_TIMELINE
            Type.MAIN -> Send.Connect.Type.MAIN
        }

        val id = typeIdMap[type]?: UUID.randomUUID().toString()
        typeIdMap = typeIdMap.toMutableMap().also {
            it[type] = id
        }
        return socket.send(Send.Connect(Send.Connect.Body(channel = body, id = id)).toJson()).also {
            logger.debug("channel=$body API登録完了 result=$it, typeIdMap=${typeIdMap}, hash=${this.hashCode()}")
        }
    }


    private fun trySendDisconnect(type: Type) {
        if(listenersMap[type].isNullOrEmpty()){
            val map = typeIdMap.toMutableMap()
            val id = map.remove(type)
            typeIdMap = map
            if(id != null){
                socket.send((Send.Disconnect(Send.Disconnect.Body(id)).toJson()))
            }

            logger.debug("channel 購読解除, type=$type, id=$id")
        }
    }


    override fun onStateChanged(e: Socket.State) {
        if(e is Socket.State.Connected) {
            val types = typeIdMap.keys
            val sendCount = types.toList().count {
                //logger.debug("接続処理: $it")
                sendConnect(it)
            }
            logger.debug("types: $typeIdMap, 送信済み数:$sendCount")
        }

    }

}