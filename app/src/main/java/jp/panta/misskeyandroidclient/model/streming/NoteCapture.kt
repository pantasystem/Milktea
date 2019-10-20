package jp.panta.misskeyandroidclient.model.streming

import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NoteCapture(
    var myUserId: String
) : Observer{


    interface NoteRemoveListener{
        fun onRemoved(id: String)
    }

    //toShowNoteのIDを登録する
    private class NoteEvent{
        val notes = ArrayList<PlaneNoteViewData>()
        fun add(planeNoteViewData: PlaneNoteViewData): NoteEvent{
            notes.add(planeNoteViewData)
            return this
        }
    }

    private data class CaptureRequest(override val type: String = "sn", val body: CaptureRequestBody): StreamingAction
    private data class CaptureRequestBody(private val id: String)

    private data class NoteUpdated(override val type: String, val body: Body<NoteUpdatedBody>): StreamingAction
    private data class NoteUpdatedBody(val reaction: String?, val userId: String?, val choice: Int?, val deletedAt: String?)

    //noteId : NoteEvent
    private val observeNoteMap = HashMap<String, NoteEvent>()

    private var noteRemoved: (id: String)->Unit = {}

    override var streamingAdapter: StreamingAdapter? = null

    private val gson = Gson()

    private val noteRemovedListeners = ArrayList<NoteRemoveListener>()

    override fun onConnect() {
        observeNoteMap.forEach{
            streamingAdapter?.send(gson.toJson(createCaptureRequest(it.key)))
        }
    }

    override fun onDissconnect() {

    }

    override fun onReceived(msg: String) {

        try{
            val receivedObject = gson.fromJson(msg, NoteUpdated::class.java)
            val id = receivedObject.body.id
            val userId = receivedObject.body.body?.userId
            val isMyReaction = myUserId == userId
            val reaction = receivedObject.body.body?.reaction
            //val isRemoved = receivedObject.body.body?.deletedAt != null


            when {
                receivedObject.body.type == "deleted" -> noteRemovedListeners.forEach{
                    it.onRemoved(id)
                }
                receivedObject.body.type == "reacted" -> addReaction(id, reaction!!, isMyReaction)
                receivedObject.body.type == "unreacted" -> removeReaction(id, reaction!!, isMyReaction)
                else -> Log.d("NoteCapture", "不明なイベント")
            }

            Log.d("NoteCapture", "onReceived: $receivedObject")
        }catch(e: JsonSyntaxException){
            //他のイベントが流れてくるので回避する
        }



    }

    fun add(planeNoteViewData: PlaneNoteViewData){
        val key = planeNoteViewData.toShowNote.id
        synchronized(observeNoteMap){
            val noteEvent = observeNoteMap[key]
            if(noteEvent == null){
                val newEvent = NoteEvent().add(planeNoteViewData)
                observeNoteMap[key] = newEvent
                streamingAdapter?.send(gson.toJson(createCaptureRequest(key)))
            }else{
                noteEvent.add(planeNoteViewData)
            }
        }

    }

    fun addAll(planeNoteViewDataList: List<PlaneNoteViewData>){
        planeNoteViewDataList.forEach{
            add(it)
        }
    }

    fun addNoteRemoveListener(listener: NoteRemoveListener){
        noteRemovedListeners.add(listener)
    }

    private fun createCaptureRequest(noteId: String): CaptureRequest{
        return CaptureRequest(body = CaptureRequestBody(noteId))
    }

    private fun addReaction(noteId: String, reaction: String, isMyReaction: Boolean){
        synchronized(observeNoteMap){
            observeNoteMap[noteId]?.notes?.forEach {
                it.addReaction(reaction, isMyReaction)
            }
        }
    }

    private fun removeReaction(noteId: String, reaction: String, isMyReaction: Boolean){
        synchronized(observeNoteMap){
            observeNoteMap[noteId]?.notes?.forEach {
                it.takeReaction(reaction, isMyReaction)
            }
        }
    }


}
