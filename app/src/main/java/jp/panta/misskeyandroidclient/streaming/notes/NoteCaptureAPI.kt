package jp.panta.misskeyandroidclient.streaming.notes

import com.google.gson.Gson
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.streaming.*
import jp.panta.misskeyandroidclient.streaming.network.StreamingEventListener
import jp.panta.misskeyandroidclient.streaming.network.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.json.Json

class NoteCaptureAPI(
    val socket: Socket,
) : Reconnectable, StreamingEventListener {



    @ExperimentalCoroutinesApi
    fun capture(noteId: String): Flow<NoteUpdated.Body> {

        return channelFlow {
            val listenId = UUID.randomUUID().toString()
            capture(noteId, listenId){ noteUpdated ->
                offer(noteUpdated.body)
            }

            awaitClose {
                unSubscribe(noteId, listenId)
            }
        }

    }



    private val noteIdListenMap = ConcurrentHashMap<String, ConcurrentHashMap<String, (NoteUpdated)->Unit>>()

    private fun capture(noteId: String, listenId: String, listener: (NoteUpdated)->Unit) {
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

    override fun handle(e: StreamingEvent): Boolean {
        if(e is NoteUpdated) {
            synchronized(noteIdListenMap) {
                noteIdListenMap[e.body.id]?.values?.forEach {
                    it.invoke(e)
                }
            }
            return true
        }
        return false
    }

    private fun sendSub(noteId: String) : Boolean{
        return socket.send(
            Send.SubscribeNote(Send.SubscribeNote.Body(noteId)).toJson()
        )
    }

    private fun sendUnSub(noteId: String) : Boolean{
        return socket.send(
            Send.UnSubscribeNote(Send.UnSubscribeNote.Body(noteId)).toJson()
        )
    }

}

