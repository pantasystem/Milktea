package net.pantasystem.milktea.api_streaming

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.Logger

interface NoteCaptureAPI {
    fun capture(noteId: String): Flow<NoteUpdated.Body>
    fun count(): Int
    fun isEmpty(): Boolean
    fun isCaptured(noteId: String): Boolean
}

class NoteCaptureAPIImpl(
    val socket: Socket,
    loggerFactory: Logger.Factory? = null,
) : SocketMessageEventListener, SocketStateEventListener, NoteCaptureAPI {


    val logger = loggerFactory?.create("NoteCaptureAPI")


    override fun capture(noteId: String): Flow<NoteUpdated.Body> {

        return channelFlow {

            val listener: (NoteUpdated) -> Unit = { noteUpdated ->
                trySend(noteUpdated.body)
            }
            capture(noteId, listener)

            awaitClose {
                unSubscribe(noteId, listener)
            }
        }

    }

    /**
     * subscribeしているノート数を計算します
     */
    override fun count(): Int = noteIdListenMap.size


    override fun isEmpty(): Boolean {
        return count() == 0
    }

    /**
     * すでにCapture済みかをチェックします
     */
    override fun isCaptured(noteId: String): Boolean {
        return noteIdListenMap.containsKey(noteId)


    }

    private var noteIdListenMap = mapOf<String, Set<(NoteUpdated) -> Unit>>()
    private val lock = Mutex()

    init {
        socket.addStateEventListener(this)
    }

    private fun capture(noteId: String, listener: (NoteUpdated) -> Unit) = runBlocking {
        lock.withLock {
            val listeners = noteIdListenMap.getOrNew(noteId)
            if (noteIdListenMap.isEmpty()) {
                socket.addMessageEventListener(this@NoteCaptureAPIImpl)
            }
            if (listeners.isEmpty()) {
                if (!sendSub(noteId)) {
                    return@runBlocking
                }
            }
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
            noteIdListenMap = noteIdListenMap.toMutableMap().also {
                it[noteId] = listeners
            }
        }


    }

    private fun unSubscribe(noteId: String, listener: (NoteUpdated) -> Unit) = runBlocking {
        lock.withLock {
            val listeners = noteIdListenMap.getOrNew(noteId)
            if (listeners.isEmpty()) {
                return@runBlocking
            }

            if (listeners.remove(listener) && listeners.isEmpty()) {
                sendUnSub(noteId)
            }
            if (listeners.isEmpty()) {
                noteIdListenMap = noteIdListenMap.toMutableMap().also {
                    it.remove(noteId)
                }
            }
            if (noteIdListenMap.isEmpty()) {
                socket.removeMessageEventListener(this@NoteCaptureAPIImpl)
            }
        }


    }


    private fun Map<String, Set<(NoteUpdated) -> Unit>>.getOrNew(noteId: String): MutableSet<(NoteUpdated) -> Unit> {
        val listeners = this[noteId]
        return listeners?.toMutableSet() ?: mutableSetOf()
    }


    override fun onMessage(e: StreamingEvent): Boolean {
        if (e is NoteUpdated) {
            logger?.debug("noteUpdated: $e")
            val listeners = if (noteIdListenMap[e.body.id].isNullOrEmpty()) {
                logger?.warning("listenerは未登録ですが、何か受信したようです。")
                null
            } else {
                noteIdListenMap[e.body.id]
            }
            listeners?.forEach {
                it.invoke(e)
            }
            return true
        }
        return false
    }

    override fun onStateChanged(e: Socket.State) {
        logger?.debug("onStateChanged $e")
        if (e is Socket.State.Connected) {
            noteIdListenMap.keys.forEach {
                sendSub(it)
            }
        } else if (e is Socket.State.Closing) {

            noteIdListenMap.keys.forEach {
                sendUnSub(it)
            }

        }
    }

    private fun sendSub(noteId: String): Boolean {
        logger?.debug("購読メッセージ送信 noteId: $noteId")
        return socket.send(
            Send.SubscribeNote(Send.SubscribeNote.Body(noteId)).toJson()
        )
    }

    private fun sendUnSub(noteId: String): Boolean {
        logger?.debug("NoteのRemoteへの購読を解除します noteId:$noteId")
        return socket.send(
            Send.UnSubscribeNote(Send.UnSubscribeNote.Body(noteId)).toJson()
        )
    }

}

