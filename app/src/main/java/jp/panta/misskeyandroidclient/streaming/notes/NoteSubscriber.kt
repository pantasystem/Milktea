package jp.panta.misskeyandroidclient.streaming.notes

import com.google.gson.Gson
import io.reactivex.Observable
import jp.panta.misskeyandroidclient.streaming.Reconnectable
import jp.panta.misskeyandroidclient.streaming.Send
import jp.panta.misskeyandroidclient.streaming.SendBody
import jp.panta.misskeyandroidclient.streaming.network.ConnectionManager
import jp.panta.misskeyandroidclient.streaming.network.MessageReceiveListener
import jp.panta.misskeyandroidclient.streaming.network.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet
import jp.panta.misskeyandroidclient.streaming.NoteUpdated

class NoteSubscriber(
    val socket: Socket,
    val gson: Gson
) : Reconnectable, MessageReceiveListener {

    @ExperimentalCoroutinesApi
    fun subscribe(noteId: String): Flow<NoteUpdated> {

        return channelFlow {
            val listenId = UUID.randomUUID().toString()
            subscribe(noteId, listenId){ noteUpdated ->
                offer(noteUpdated)
            }

            awaitClose {
                unSubscribe(noteId, listenId)
            }
        }

    }



    private val noteIdListenMap = ConcurrentHashMap<String, ConcurrentHashMap<String, (NoteUpdated)->Unit>>()

    private fun subscribe(noteId: String, listenId: String ,listener: (NoteUpdated)->Unit) {
        synchronized(noteIdListenMap){
            val listeners = noteIdListenMap.getOrNew(noteId)
            if(listeners.isEmpty()){
                if(sendSub(noteId)){
                    return@synchronized
                }
            }
            if(listeners.contains(listenId)) {
                listeners[listenId] = listener
            }
            noteIdListenMap[noteId] = listeners

        }

    }

    private fun unSubscribe(noteId: String, listenId: String) {
        synchronized(noteIdListenMap){

            val listeners = noteIdListenMap.getOrNew(noteId)
            if(listeners.isEmpty()) {
                return
            }

            if(listeners.remove(listenId) != null && listeners.isEmpty()) {
                sendUnSub(noteId)
            }
            noteIdListenMap[noteId] = listeners
        }

    }


    private fun Map<String, ConcurrentHashMap<String, (NoteUpdated)->Unit>>.getOrNew(noteId: String) : ConcurrentHashMap<String, (NoteUpdated)->Unit> {
        val listeners = this[noteId]
        return listeners ?: ConcurrentHashMap<String, (NoteUpdated)->Unit>()
    }

    override fun onReconnect() {
        synchronized(noteIdListenMap) {
            noteIdListenMap.keys.forEach {
                sendSub(it)
            }
        }
    }

    override fun onReceiveMessage(message: String): Boolean {
        // TODO メッセージを受信したときの処理を実装する
        return false
    }

    private fun sendSub(noteId: String) : Boolean{
        return socket.send(
            gson.toJson(
                Send(
                    SendBody.SubscribeNote(noteId)
                )
            )
        )
    }

    private fun sendUnSub(noteId: String) : Boolean{
        return socket.send(
            gson.toJson(
                SendBody.UnSubscribeNote(noteId)
            )
        )
    }

}

