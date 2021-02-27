package jp.panta.misskeyandroidclient.streaming.notes

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.streaming.*
import jp.panta.misskeyandroidclient.streaming.SocketEventListener
import jp.panta.misskeyandroidclient.streaming.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NoteCaptureAPI(
    val socket: Socket,
    loggerFactory: Logger.Factory? = null
) : SocketEventListener {


    val logger = loggerFactory?.create("NoteCaptureAPI")

    init {
        socket.addSocketEventListener(this)
    }

    @ExperimentalCoroutinesApi
    fun capture(noteId: String): Flow<NoteUpdated.Body> {

        return channelFlow {
            logger?.debug("channelFlow起動")
            val listenId = UUID.randomUUID().toString()
            capture(noteId, listenId){ noteUpdated ->
                logger?.debug("受信:$noteUpdated")
                offer(noteUpdated.body)
            }

            awaitClose {
                logger?.debug("captureを終了する noteId=$noteId, listenId=$listenId")
                unSubscribe(noteId, listenId)
            }
        }

    }



    private val noteIdListenMap = ConcurrentHashMap<String, ConcurrentHashMap<String, (NoteUpdated)->Unit>>()

    private fun capture(noteId: String, listenId: String, listener: (NoteUpdated)->Unit) {
        synchronized(noteIdListenMap){
            val listeners = noteIdListenMap.getOrNew(noteId)
            if(listeners.isEmpty()){
                logger?.debug("リモートへCaptureができていなかったので開始する")
                if(!sendSub(noteId)){
                    return@synchronized
                }
            }
            if(!listeners.contains(listenId)) {
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



    override fun onMessage(e: StreamingEvent): Boolean {
        if(e is NoteUpdated) {
            synchronized(noteIdListenMap) {
                if(noteIdListenMap[e.body.id].isNullOrEmpty()) {
                    logger?.warning("listenerは未登録ですが、何か受信したようです。")
                }else{
                    noteIdListenMap[e.body.id]?.values?.forEach {
                        it.invoke(e)
                    }
                }

            }
            return true
        }
        return false
    }

    override fun onStateChanged(e: Socket.State) {
        if(e is Socket.State.Connected) {
            synchronized(noteIdListenMap) {
                noteIdListenMap.keys.forEach {
                    sendSub(it)
                }
            }
        }else if(e is Socket.State.Closing) {
            synchronized(noteIdListenMap) {
                noteIdListenMap.keys.forEach {
                    sendUnSub(it)
                }
            }
        }
    }

    private fun sendSub(noteId: String) : Boolean{
        return socket.send(
            Send.SubscribeNote(Send.SubscribeNote.Body(noteId)).toJson()
        )
    }

    private fun sendUnSub(noteId: String) : Boolean{
        logger?.debug("NoteのRemoteへの購読を解除します noteId:$noteId")
        return socket.send(
            Send.UnSubscribeNote(Send.UnSubscribeNote.Body(noteId)).toJson()
        )
    }

}

