package jp.panta.misskeyandroidclient.streaming.notes

import com.google.gson.Gson
import io.reactivex.Observable
import jp.panta.misskeyandroidclient.streaming.network.ConnectionManager
import jp.panta.misskeyandroidclient.streaming.network.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

class NoteSubscriber(
    val socket: Socket,
    val connectionManager: ConnectionManager,
    val gson: Gson
) {

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
    private data class Listener(
        val listenId: String,
        val listener: (NoteUpdated)-> Unit
    )


    private val noteIdListenMap = ConcurrentHashMap<String, ConcurrentHashMap<String, Listener>>()

    private fun subscribe(noteId: String, listenId: String ,listener: (NoteUpdated)->Unit) {
        val listeners = noteIdListenMap.getOrNew(noteId)
        if(listeners.isEmpty()){
            // TODO remoteに対してsubscribeする
        }else if(listeners.contains(listenId)) {
            listeners[listenId] = Listener(listenId, listener)
        }
        noteIdListenMap[noteId] = listeners
    }

    private fun unSubscribe(noteId: String, listenId: String) {
        val listeners = noteIdListenMap.getOrNew(noteId)
        if(listeners.isEmpty()) {
            return
        }

        if(listeners.remove(listenId) != null && listeners.isEmpty()) {
            // TODO remoteへのsubscribeを解除する
        }
        noteIdListenMap[noteId] = listeners
    }


    private fun Map<String, ConcurrentHashMap<String, Listener>>.getOrNew(noteId: String) : ConcurrentHashMap<String, Listener> {
        val listeners = this[noteId]
        return listeners ?: ConcurrentHashMap<String, Listener>()
    }

}

