package jp.panta.misskeyandroidclient.streaming.notes

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.streaming.*
import jp.panta.misskeyandroidclient.streaming.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

class NoteCaptureAPI(
    val socket: Socket,
    loggerFactory: Logger.Factory? = null
) : SocketMessageEventListener, SocketStateEventListener {


    val logger = loggerFactory?.create("NoteCaptureAPI")



    @ExperimentalCoroutinesApi
    fun capture(noteId: String): Flow<NoteUpdated.Body> {

        return channelFlow {
            logger?.debug("channelFlow起動")
            val listenId = UUID.randomUUID().toString()

            val listener: (NoteUpdated)-> Unit = { noteUpdated ->
                logger?.debug("受信:$noteUpdated")
                offer(noteUpdated.body)
            }
            //logger?.debug("before capture")
            capture(noteId, listener)
            //logger?.debug("after capture")

            awaitClose {
                logger?.debug("captureを終了する noteId=$noteId, listenId=$listenId")
                unSubscribe(noteId, listener)
            }
        }

    }

    /**
     * subscribeしているノート数を計算します
     */
    fun count(): Int {
        synchronized(noteIdListenMap) {
            return noteIdListenMap.size
        }
    }

    fun isEmpty(): Boolean {
        return count() == 0
    }

    /**
     * すでにCapture済みかをチェックします
     */
    fun isCaptured(noteId: String): Boolean {
        synchronized(noteIdListenMap) {
            return noteIdListenMap.containsKey(noteId)
        }
    }

    private val noteIdListenMap = ConcurrentHashMap<String, HashSet<(NoteUpdated)->Unit>>()

    private fun capture(noteId: String, listener: (NoteUpdated)->Unit) {
        synchronized(noteIdListenMap){
            val listeners = noteIdListenMap.getOrNew(noteId)
            if(noteIdListenMap.isEmpty()) {
                socket.addMessageEventListener(this)
                socket.addStateEventListener(this)
            }
            if(listeners.isEmpty()){
                logger?.debug("リモートへCaptureができていなかったので開始する")
                if(!sendSub(noteId)){
                    return@synchronized
                }
            }
            if(!listeners.contains(listener)) {
                listeners.add(listener)
            }
            noteIdListenMap[noteId] = listeners

        }

    }

    private fun unSubscribe(noteId: String, listener: (NoteUpdated) -> Unit) {
        synchronized(noteIdListenMap){

            logger?.debug("unSubscribe noteId: $noteId")
            val listeners = noteIdListenMap.getOrNew(noteId)
            if(listeners.isEmpty()) {
                return
            }

            if(listeners.remove(listener)  && listeners.isEmpty()) {
                sendUnSub(noteId)
            }
            if(listeners.isEmpty()) {
                noteIdListenMap.remove(noteId)
            }
            if(noteIdListenMap.isEmpty()) {
                socket.removeMessageEventListener(this)
                socket.removeStateEventListener(this)
            }
        }

    }


    private fun Map<String, HashSet<(NoteUpdated)->Unit>>.getOrNew(noteId: String) : HashSet<(NoteUpdated)->Unit> {
        val listeners = this[noteId]
        return listeners ?: HashSet()
    }



    override fun onMessage(e: StreamingEvent): Boolean {
        if(e is NoteUpdated) {
            logger?.debug("noteUpdated: $e")
            synchronized(noteIdListenMap) {
                if(noteIdListenMap[e.body.id].isNullOrEmpty()) {
                    logger?.warning("listenerは未登録ですが、何か受信したようです。")
                }else{
                    noteIdListenMap[e.body.id]?.forEach {
                        it.invoke(e)
                    }
                }

            }
            return true
        }
        return false
    }

    override fun onStateChanged(e: Socket.State) {
        logger?.debug("onStateChanged $e")
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
        logger?.debug("購読メッセージ送信 noteId: $noteId")
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

