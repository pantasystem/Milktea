package jp.panta.misskeyandroidclient.streaming.channel

import com.google.gson.Gson
import jp.panta.misskeyandroidclient.streaming.Send
import jp.panta.misskeyandroidclient.streaming.SendBody
import jp.panta.misskeyandroidclient.streaming.network.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChannelAPI(
    val socket: Socket,
    val gson: Gson
) {

    enum class Type {
        MAIN, HOME, LOCAL, HYBRID, GLOBAL
    }



    private val listenersMap = ConcurrentHashMap<Type, HashSet<String>>(
        mapOf(
            Type.MAIN to hashSetOf(),
            Type.HOME to hashSetOf(),
            Type.LOCAL to hashSetOf(),
            Type.HYBRID to hashSetOf(),
            Type.GLOBAL to hashSetOf()
        )
    )

    private val typeIdMap = ConcurrentHashMap<Type, String>()

    @ExperimentalCoroutinesApi
    fun connect(type: Type) : Flow<ChannelEvent> {
        return channelFlow {
            val listenId = UUID.randomUUID().toString()
            connect(type, listenId){
                offer(it)
            }

            awaitClose {
                disconnect(type, listenId)
            }
        }
    }

    private fun connect(type: Type, listenId: String, ev: (ChannelEvent)->Unit) {
        // TODO メッセージを受信したときの処理を実装する
        synchronized(listenersMap) {
            if(listenersMap[type]?.contains(listenId) == true){
                return@synchronized
            }

            val body = when(type){
                Type.GLOBAL -> SendBody.Connect.GlobalTimeline()
                Type.HYBRID -> SendBody.Connect.HybridTimeline()
                Type.LOCAL -> SendBody.Connect.LocalTimeline()
                Type.HOME -> SendBody.Connect.HomeTimeline()
                Type.MAIN -> SendBody.Connect.Main()
            }
            val id = body.id
            typeIdMap[type] = id
            socket.send(gson.toJson(
                Send(body)
            ))
        }
    }

    private fun disconnect(type: Type, listenId: String) {
        synchronized(listenersMap) {
            if(listenersMap[type]?.contains(listenId) != true){
                return@synchronized
            }

            val id = typeIdMap[type]
            if(id != null){
                socket.send(gson.toJson(Send(SendBody.Disconnect(id))))
            }
        }
    }

}