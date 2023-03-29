package net.pantasystem.milktea.api_streaming.channel

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api_streaming.*
import net.pantasystem.milktea.common.Logger
import java.util.*

class ChannelAPI(
    val socket: Socket,
    loggerFactory: Logger.Factory,
) : SocketMessageEventListener, SocketStateEventListener {


    sealed interface Type {
        object Main : Type
        object Home : Type
        object Local : Type
        object Hybrid : Type
        object Global : Type
        data class UserList(
            val userListId: String
        ) : Type

        data class Antenna(
            val antennaId: String
        ) : Type

        data class Channel(
            val channelId: String
        ) : Type
    }

    private val logger = loggerFactory.create("ChannelAPI")

    private var listenersMap = mapOf<Type, Set<(ChannelBody) -> Unit>>(
        Type.Main to hashSetOf(),
        Type.Home to hashSetOf(),
        Type.Local to hashSetOf(),
        Type.Hybrid to hashSetOf(),
        Type.Global to hashSetOf(),
    )

    private var typeIdMap = mapOf<Type, String>()
    private val mutex = Mutex()

    init {
        //socket.addMessageEventListener(this)
        socket.addStateEventListener(this)
    }

    fun connect(type: Type): Flow<ChannelBody> {
        return channelFlow {
            val callback: (ChannelBody) -> Unit = {
                trySend(it).onFailure {  e ->
                    logger.error("ChannelAPI Streamingデータの伝達に失敗", e)
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


    private fun connect(type: Type, listener: (ChannelBody) -> Unit): Unit = runBlocking {
        // NOTE すでにlistenerを追加済みであれば何もせずに終了する。
        mutex.withLock {
            if (listenersMap[type]?.contains(listener) == true) {
                logger.debug { "リッスン済み" }
                return@runBlocking
            }

            fun getOrNew(type: Type): Set<(ChannelBody) -> Unit> {
                return listenersMap[type] ?: emptySet()
            }

            val sets = getOrNew(type).toMutableSet().also {
                it.add(listener)
            }
            listenersMap = listenersMap.toMutableMap().also {
                it[type] = sets
            }

            if (typeIdMap.isEmpty()) {
                socket.addMessageEventListener(this@ChannelAPI)
            }
            if (typeIdMap[type] == null) {
                logger.debug { "接続処理を開始" }
                sendConnect(type)
                logger.debug { "after sendConnect:${typeIdMap}" }
            }
        }

    }

    private fun disconnect(type: Type, listener: (ChannelBody) -> Unit) = runBlocking {
        mutex.withLock {
            if (listenersMap[type]?.contains(listener) != true) {
                return@runBlocking
            }

            listenersMap = listenersMap.toMutableMap().also {
                it[type] = (it[type]?.toMutableSet() ?: emptySet()).toMutableSet().also { set ->
                    set.remove(listener)
                }
            }

            // 誰にも使われていなければサーバーからChannelへの接続を開放する
            trySendDisconnect(type)
            if (typeIdMap.isEmpty()) {
                socket.removeMessageEventListener(this@ChannelAPI)
            }
        }

    }


    override fun onMessage(e: StreamingEvent): Boolean {
        if (e is ChannelEvent) {
            typeIdMap.filter {
                it.value == e.body.id
            }.keys.forEach {
                listenersMap[it]?.forEach { callback ->
                    callback.invoke(e.body)
                } ?: throw IllegalStateException("未実装なTypeです。")
            }

            return true
        }

        return false
    }

    /**
     * 接続メッセージを現在の状態にかかわらずサーバーに送信する
     */
    private fun sendConnect(type: Type): Boolean {
        logger.debug { "sendConnect($type)" }
        val body = when (type) {
            is Type.Global -> Send.Connect.Type.GLOBAL_TIMELINE
            is Type.Hybrid -> Send.Connect.Type.HYBRID_TIMELINE
            is Type.Local -> Send.Connect.Type.LOCAL_TIMELINE
            is Type.Home -> Send.Connect.Type.HOME_TIMELINE
            is Type.Main -> Send.Connect.Type.MAIN
            is Type.UserList -> Send.Connect.Type.USER_LIST
            is Type.Antenna -> Send.Connect.Type.ANTENNA
            is Type.Channel -> Send.Connect.Type.CHANNEL
        }

        val id = typeIdMap[type] ?: UUID.randomUUID().toString()
        typeIdMap = typeIdMap.toMutableMap().also {
            it[type] = id
        }
        return socket.send(
            Send.Connect(
                Send.Connect.Body(
                    channel = body,
                    id = id,
                    params = Send.Connect.Body.Params(
                        listId = (type as? Type.UserList)?.userListId,
                        antennaId = (type as? Type.Antenna)?.antennaId,
                        channelId = (type as? Type.Channel)?.channelId
                    )
                )
            ).toJson()
        ).also {
            logger.debug { "channel=$body API登録完了 result=$it, typeIdMap=${typeIdMap}, hash=${this.hashCode()}" }
        }
    }


    private fun trySendDisconnect(type: Type) {
        if (listenersMap[type].isNullOrEmpty()) {
            val map = typeIdMap.toMutableMap()
            val id = map.remove(type)
            typeIdMap = map
            if (id != null) {
                socket.send((Send.Disconnect(Send.Disconnect.Body(id)).toJson()))
            }

            logger.debug { "channel 購読解除, type=$type, id=$id" }
        }
    }


    override fun onStateChanged(e: Socket.State) {
        if (e is Socket.State.Connected) {
            val types = typeIdMap.keys
            val sendCount = types.toList().count {
                //logger.debug("接続処理: $it")
                sendConnect(it)
            }
            logger.debug { "types: $typeIdMap, 送信済み数:$sendCount" }
        }

    }

}
