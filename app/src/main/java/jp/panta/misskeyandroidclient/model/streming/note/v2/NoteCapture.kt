package jp.panta.misskeyandroidclient.model.streming.note.v2

import android.util.Log
import com.google.gson.JsonSyntaxException
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.streming.AbsObserver
import jp.panta.misskeyandroidclient.model.streming.Body
import jp.panta.misskeyandroidclient.model.streming.StreamingAction
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

/**
 * リモートのノートのキャプチャーを行い
 * リモートの更新イベントをイベントデータとして返す
 *
 */
class NoteCapture(override val account: Account, val noteRepositoryFactory: NoteRepository.Factory) : AbsObserver(){
    data class CaptureRequest(override val type: String = "sn", val body: CaptureRequestBody): StreamingAction
    data class CaptureRequestBody(private val id: String)

    data class NoteUpdated(override val type: String, val body: Body<NoteUpdatedBody>): StreamingAction
    data class NoteUpdatedBody(val reaction: String?, val userId: String?, val choice: Int?, val deletedAt: String?, val emoji: Emoji?)

    class Client {
        val clientId: String = UUID.randomUUID().toString()

        var noteCapture: NoteCapture? = null

        private val captureNotes = HashSet<String>()

        val subject = PublishSubject.create<NoteEvent>()

        fun capture(noteId: String): Boolean{
            return noteCapture?.capture(this, noteId)?: false
        }

        fun unCapture(noteId: String): Boolean{
            return noteCapture?.unCapture(this, noteId)?: false
        }

        fun getObservable(): Observable<NoteEvent>{
            return subject
        }

        fun getCaptureNotes(): Set<String>{
            return captureNotes
        }


    }

    private val mGson = GsonFactory.create()

    override var streamingAdapter: StreamingAdapter? = null

    private val notesClients = ConcurrentHashMap<String, Set<String>>()

    private val clients = ConcurrentHashMap<String, Client>()


    fun putClient(client: Client){
        val added = clients[client.clientId]
        if(added == null){
            clients[client.clientId] = client
            client.noteCapture = this
            client.getCaptureNotes().forEach { note->
                capture(client, note)
            }
        }
    }

    fun removeClient(client: Client){
        val capturedClient = clients.remove(client.clientId)
            ?: return

        capturedClient.getCaptureNotes().forEach { note ->
            unCapture(capturedClient, note)
        }
        capturedClient.noteCapture = null
    }



    fun capture(client: Client, noteId: String): Boolean{
        val clients = HashSet<String>(notesClients[noteId]?: HashSet()?: emptySet())

        if(!clients.contains(client.clientId)){
            val added = clients.add(client.clientId)
            captureRemote(noteId)
            return added
        }
        return notesClients.put(noteId, clients) != null

    }

    fun unCapture(client: Client, noteId: String): Boolean{
        val clients = HashSet<String>(notesClients[noteId]?: emptySet())
        val a = clients.remove(client.clientId)
        if(clients.isEmpty() && a){
            unCaptureRemote(noteId)
        }

        notesClients[noteId] = clients
        return a

    }

    private fun captureRemote(noteId: String){
        streamingAdapter?.send(mGson.toJson(createCaptureRequest(noteId))).let{
            if(it == null || it == false){
                Log.w("NoteCapture", "ノートの登録に失敗した")
            }

        }
    }

    private fun unCaptureRemote(noteId: String){
        streamingAdapter?.send(mGson.toJson(createUnCaptureRequest(noteId)))
    }

    private fun createCaptureRequest(noteId: String): CaptureRequest {
        return CaptureRequest(body = CaptureRequestBody(noteId))
    }

    private fun createUnCaptureRequest(noteId: String): CaptureRequest {
        return CaptureRequest(
            type = "unsubNote",
            body = CaptureRequestBody(noteId)
        )
    }

    override fun onReceived(msg: String) {
        try{
            val receivedObject = mGson.fromJson(msg, NoteUpdated::class.java)
            val id = receivedObject.body.id
            val userId = receivedObject.body.body?.userId
            val reaction = receivedObject.body.body?.reaction


            val body = when (
                receivedObject.body.type) {
                "reacted" -> Event.Reacted(reaction = reaction!!, userId = userId, emoji = receivedObject.body.body.emoji)
                "unreacted" -> Event.UnReacted(reaction = reaction!!, userId = userId)
                "pollVoted" -> Event.Voted(receivedObject.body.body?.choice!!, userId = userId)
                "deleted" -> Event.Deleted
                else -> return
            }
            val noteEvent = NoteEvent(
                noteId = id,
                event = body
            )

            onUpdate(noteEvent)

            if(receivedObject.body.type == "reacted"){
                Log.d("NoteCapture", "onReceived: $receivedObject")
            }
        }catch(e: JsonSyntaxException){
            //他のイベントが流れてくるので回避する
        }catch(e: Exception){

        }
    }

    private fun onUpdate(noteEvent: NoteEvent){
        val clientIds = notesClients[noteEvent.noteId]
        clientIds?.forEach{ clientId ->
            clients[clientId]?.subject?.onNext(noteEvent)
        }
    }




    override fun onClosing() {
        notesClients.keys.forEach{ noteId ->
            unCaptureRemote(noteId)
        }
    }

    override fun onConnect() {
        notesClients.keys.forEach{ noteId ->
            captureRemote(noteId)
        }
    }

    override fun onDisconnect() = Unit


}