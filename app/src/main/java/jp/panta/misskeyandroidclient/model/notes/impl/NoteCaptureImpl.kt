package jp.panta.misskeyandroidclient.model.notes.impl


import android.util.Log
import com.google.gson.JsonSyntaxException
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.streming.AbsObserver
import jp.panta.misskeyandroidclient.model.streming.Body
import jp.panta.misskeyandroidclient.model.streming.StreamingAction
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import kotlinx.coroutines.flow.Flow
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

/**
 * リモートのノートのキャプチャーを行い
 * リモートの更新イベントをイベントデータとして返す
 *
 */
class NoteCaptureImpl :  NoteCapture{
    data class CaptureRequest(override val type: String = "sn", val body: CaptureRequestBody): StreamingAction
    data class CaptureRequestBody(private val id: String)

    data class NoteUpdated(override val type: String, val body: Body<NoteUpdatedBody>): StreamingAction
    data class NoteUpdatedBody(val reaction: String?, val userId: String?, val choice: Int?, val deletedAt: String?, val emoji: Emoji?)


    override fun capture(noteId: Note.Id): Boolean {
        TODO("Not yet implemented")
    }

    override fun unCapture(noteId: Note.Id): Boolean {
        TODO("Not yet implemented")
    }

    override fun observer(): Flow<NoteCaptureEvent> {
        TODO("Not yet implemented")
    }



    private val mGson = GsonFactory.create()

    override var streamingAdapter: StreamingAdapter? = null

    /**
     * noteIdとそのNoteをキャプチャーしているClientのclientId
     */
    private val noteIdsClients = ConcurrentHashMap<String, Set<String>>()

    /**
     * clientIdをKey、ClientをValueとするHashMap
     */
    private val clients = ConcurrentHashMap<String, Client>()




    fun capture(client: Client, noteId: String): Boolean{

        synchronized(noteIdsClients){
            val notesClients = HashSet<String>(noteIdsClients[noteId]?: HashSet()?: emptySet())

            try{
                if(!notesClients.contains(client.clientId)){
                    val added = notesClients.add(client.clientId)
                    captureRemote(noteId)
                    return added
                }
                return this.noteIdsClients.put(noteId, notesClients) != null
            }catch(e: Exception){
                Log.d("NoteCapture", "capture error",e)
                return false
            }finally{
                noteIdsClients[noteId] = notesClients
            }
        }



    }


    fun unCapture(client: Client, noteId: String): Boolean{
        synchronized(noteIdsClients){
            val notesClients = HashSet<String>(noteIdsClients[noteId]?: emptySet())
            val a = notesClients.remove(client.clientId)
            if(notesClients.isEmpty() && a){
                unCaptureRemote(noteId)
            }

            this.noteIdsClients[noteId] = notesClients
            return a
        }

    }

    /**
     * キャプチャー状態にかかわらずキャプチャーメッセージをサーバーへ転送します。
     */
    fun sendUnCapture(noteId: String) {

    }

    /**
     * キャプチャー状態にかかわらずキャプチャーメッセージをサーバーへ転送します。
     */
    fun sendCapture(noteId: Note.Id) {

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
                "reacted" -> Event.NewNote.Reacted(reaction = reaction!!, userId = userId, emoji = receivedObject.body.body.emoji)
                "unreacted" -> Event.NewNote.UnReacted(reaction = reaction!!, userId = userId)
                "pollVoted" -> Event.NewNote.Voted(receivedObject.body.body?.choice!!, userId = userId)
                "deleted" -> Event.Deleted
                else -> return
            }
            val noteEvent = NoteCaptureEvent(
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

    private fun onUpdate(noteEvent: NoteCaptureEvent){
        //val clientIds = noteIdsClients[noteEvent.noteId]
        //Log.d("onUpdate", "client数: ${clientIds?.size}, event:$noteEvent")

        noteEventStore.release(noteEvent)
    }




    override fun onClosing() {
        synchronized(noteIdsClients){
            noteIdsClients.keys.forEach { noteId ->
                unCaptureRemote(noteId)
            }
        }

    }

    override fun onConnect() {
        synchronized(noteIdsClients){
            noteIdsClients.keys.forEach{ noteId ->
                captureRemote(noteId)
            }
        }

    }

    override fun onDisconnect() = Unit


}

fun NoteCapture.Client.captureAll(notes: List<NoteDTO>): Int{
    return notes.count {
        this.capture(it.id)
    }
}

/**
 * ノートキャプチャーの送受信を行う
 * またアカウント１に対し１で対応付けられる
 */
class NoteCaptureObserver(override val account: Account) : AbsObserver(){

    override var streamingAdapter: StreamingAdapter? = null

    data class CaptureRequest(override val type: String = "sn", val body: CaptureRequestBody): StreamingAction
    data class CaptureRequestBody(private val id: String)

    data class NoteUpdated(override val type: String, val body: Body<NoteUpdatedBody>): StreamingAction
    data class NoteUpdatedBody(val reaction: String?, val userId: String?, val choice: Int?, val deletedAt: String?, val emoji: Emoji?)


    private val mGson = GsonFactory.create()


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
                "reacted" -> Event.NewNote.Reacted(reaction = reaction!!, userId = userId, emoji = receivedObject.body.body.emoji)
                "unreacted" -> Event.NewNote.UnReacted(reaction = reaction!!, userId = userId)
                "pollVoted" -> Event.NewNote.Voted(receivedObject.body.body?.choice!!, userId = userId)
                "deleted" -> Event.Deleted
                else -> return
            }


            onUpdate(id, body)

        }catch(e: JsonSyntaxException){
            //他のイベントが流れてくるので回避する
        }catch(e: Exception){

        }
    }

    private fun onUpdate(noteId: String, noteEvent: Event) {
        // TODO 上位レイヤに更新を伝える
    }




    override fun onClosing() {


    }

    override fun onConnect() {
        synchronized(noteIdsClients){
            noteIdsClients.keys.forEach{ noteId ->
                captureRemote(noteId)
            }
        }

    }

    override fun onDisconnect() = Unit
}
