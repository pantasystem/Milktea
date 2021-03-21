package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.streaming.*
import jp.panta.misskeyandroidclient.streaming.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChannelAPI(
    val socket: Socket,
) : SocketMessageEventListener, SocketStateEventListener {

    enum class Type {
        MAIN, HOME, LOCAL, HYBRID, GLOBAL
    }


    private val listenersMap = ConcurrentHashMap<Type, HashSet<(ChannelBody)->Unit>>(
        mapOf(
            Type.MAIN to hashSetOf(),
            Type.HOME to hashSetOf(),
            Type.LOCAL to hashSetOf(),
            Type.HYBRID to hashSetOf(),
            Type.GLOBAL to hashSetOf()
        )
    )

    private val typeIdMap = ConcurrentHashMap<Type, String>()

    init {
        socket.addMessageEventListener(this)
        socket.addStateEventListener(this)
    }

    @ExperimentalCoroutinesApi
    fun connect(type: Type) : Flow<ChannelBody> {
        return channelFlow {
            val callback: (ChannelBody)-> Unit = {
                offer(it)
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
    fun count(): Int {
        synchronized(listenersMap) {
            return listenersMap.filterNot {
                it.value.isNullOrEmpty()
            }.count()
        }
    }

    /**
     * 接続しているチャンネル数がゼロであるか
     */
    fun isEmpty(): Boolean {
        return count() == 0
    }

    private fun connect(type: Type, listener: (ChannelBody)->Unit) {
        synchronized(typeIdMap) {
            // NOTE すでにlistenerを追加済みであれば何もせずに終了する。
            if(listenersMap[type]?.contains(listener) == true) {
                return@synchronized
            }

            listenersMap[type]?.add(listener)
                ?: throw IllegalStateException("listenersがNULLです。")

            if(typeIdMap[type] == null){
                sendConnect(type)
            }

        }
    }



    override fun onMessage(e: StreamingEvent): Boolean {
        if(e is ChannelEvent) {
            synchronized(typeIdMap) {
                typeIdMap.filter {
                    it.value == e.body.id
                }.keys.forEach {
                    listenersMap[it]?.forEach { callback ->
                        callback.invoke(e.body)
                    }?: throw IllegalStateException("未実装なTypeです。")
                }

            }
            return true
        }

        return false
    }

    /**
     * 接続メッセージを現在の状態にかかわらずサーバーに送信する
     */
    private fun sendConnect(type: Type): Boolean {
        if(typeIdMap.isEmpty()) {
            socket.addMessageEventListener(this)
            socket.addStateEventListener(this)
        }
        val body = when(type){
            Type.GLOBAL -> Send.Connect.Type.GLOBAL_TIMELINE
            Type.HYBRID -> Send.Connect.Type.HYBRID_TIMELINE
            Type.LOCAL -> Send.Connect.Type.LOCAL_TIMELINE
            Type.HOME -> Send.Connect.Type.HOME_TIMELINE
            Type.MAIN -> Send.Connect.Type.MAIN
        }

        val id = UUID.randomUUID().toString()
        if(
            socket.send(Send.Connect(Send.Connect.Body(channel = body, id = id)).toJson())
        ){
            typeIdMap[type] = id
            return true
        }
        return false
    }

    private fun disconnect(type: Type, listener: (ChannelBody) -> Unit) {
        synchronized(listenersMap) {
            if(listenersMap[type]?.contains(listener) != true){
                return@synchronized
            }

            listenersMap[type]?.remove(listener)

            // 誰にも使われていなければサーバーからChannelへの接続を開放する
            trySendDisconnect(type)

        }
    }

    private fun trySendDisconnect(type: Type) {
        if(listenersMap[type].isNullOrEmpty()){
            val id = typeIdMap.remove(type)
            if(id != null){
                socket.send((Send.Disconnect(Send.Disconnect.Body(id)).toJson()))
            }
            if(typeIdMap.isEmpty()) {
                socket.removeMessageEventListener(this)
                socket.removeStateEventListener(this)
            }
        }
    }


    override fun onStateChanged(e: Socket.State) {
        if(e is Socket.State.Connected) {
            synchronized(typeIdMap) {
                val types = typeIdMap.keys
                typeIdMap.clear()
                types.forEach {
                    sendConnect(it)
                }
            }
        }
    }

}