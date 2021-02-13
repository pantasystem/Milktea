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


    private val listenersMap = ConcurrentHashMap<Type, HashMap<String,(ChannelEvent)->Unit>>(
        mapOf(
            Type.MAIN to hashMapOf(),
            Type.HOME to hashMapOf(),
            Type.LOCAL to hashMapOf(),
            Type.HYBRID to hashMapOf(),
            Type.GLOBAL to hashMapOf()
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

    private fun connect(type: Type, listenId: String, listener: (ChannelEvent)->Unit) {
        // TODO メッセージを受信したときの処理を実装する
        synchronized(listenersMap) {
            if(listenersMap[type]?.contains(listenId) == true){
                return@synchronized
            }

            listenersMap[type]?.let{ map ->
                map[listenId] = listener
            }
            if(typeIdMap[type] == null){
                sendConnect(type)
            }

        }
    }

    /**
     * 接続メッセージを現在の状態にかかわらずサーバーに送信する
     */
    private fun sendConnect(type: Type): Boolean {
        val body = when(type){
            Type.GLOBAL -> SendBody.Connect.GlobalTimeline()
            Type.HYBRID -> SendBody.Connect.HybridTimeline()
            Type.LOCAL -> SendBody.Connect.LocalTimeline()
            Type.HOME -> SendBody.Connect.HomeTimeline()
            Type.MAIN -> SendBody.Connect.Main()
        }

        val id = body.id
        if(
            socket.send(gson.toJson(
                Send(body)
            ))
        ){
            typeIdMap[type] = id
            return true
        }
        return false
    }

    private fun disconnect(type: Type, listenId: String) {
        synchronized(listenersMap) {
            if(listenersMap[type]?.contains(listenId) != true){
                return@synchronized
            }

            listenersMap[type]?.remove(listenId)

            // 誰にも使われていなければサーバーからChannelへの接続を開放する
            if(listenersMap[type].isNullOrEmpty()){
                val id = typeIdMap.remove(type)
                if(id != null){
                    socket.send(gson.toJson(Send(SendBody.Disconnect(id))))
                }
            }

        }
    }

    private fun sendDisconnect(type: Type) {
        if(listenersMap[type].isNullOrEmpty()){
            val id = typeIdMap.remove(type)
            if(id != null){
                socket.send(gson.toJson(Send(SendBody.Disconnect(id))))
            }
        }
    }

    /**
     * 再接続処理
     */
    fun onReconnect() {
        synchronized(listenersMap) {
            val types = typeIdMap.keys
            typeIdMap.clear()
            types.forEach {
                sendConnect(it)
            }
        }
    }

}