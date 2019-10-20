package jp.panta.misskeyandroidclient.model.streming

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import com.google.gson.Gson
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ObservableArrayListNoteCapture(
    var myUserId: String,
    var isRemoveNote: Boolean
) : Observer{


    /*
    {
    body: {id: "7z28k1rj2p", type: "reacted", body: {reaction: "🤯", userId: "7roinhytrr"},
    type: "noteUpdated"}
     */

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

    //接続失敗時、切断時にここに監視中のノート、監視しようとしていたノートがプッシュされる
    private val unObserveQueue = ArrayDeque<NoteEvent>()

    override var streamingAdapter: StreamingAdapter? = null

    private val gson = Gson()

    override fun onConnect() {
    }

    override fun onDissconnect() {

    }
    override fun onReceived(msg: String) {
        val receivedObject = gson.fromJson(msg, NoteUpdated::class.java)
        val id = receivedObject.body.id
        val userId = receivedObject.body.body?.userId
        val isMyReaction = myUserId == receivedObject.body.body?.userId
        val reaction = receivedObject.body.body?.reaction


    }

    fun addObservableArrayList(tag: String, list: ObservableArrayList<PlaneNoteViewData>, scope: CoroutineScope){
        list.addOnListChangedCallback(observableArrayListListener)
    }



    private fun add(planeNoteViewData: PlaneNoteViewData){
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

    private fun addAll(planeNoteViewDataList: List<PlaneNoteViewData>){
        planeNoteViewDataList.forEach{
            add(it)
        }
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

    private fun removeNote(noteId: String){
        if(isRemoveNote){
            GlobalScope.launch{

            }
        }
    }


    private val observableArrayListListener = object : ObservableList.OnListChangedCallback<ObservableArrayList<PlaneNoteViewData>>(){
        override fun onItemRangeInserted(
            sender: ObservableArrayList<PlaneNoteViewData>?,
            positionStart: Int,
            itemCount: Int
        ) {
            if(sender != null){
                addAll(sender.toList())
            }
        }

        override fun onChanged(sender: ObservableArrayList<PlaneNoteViewData>?) {
        }

        override fun onItemRangeChanged(
            sender: ObservableArrayList<PlaneNoteViewData>?,
            positionStart: Int,
            itemCount: Int
        ) {
        }

        override fun onItemRangeMoved(
            sender: ObservableArrayList<PlaneNoteViewData>?,
            fromPosition: Int,
            toPosition: Int,
            itemCount: Int
        ) {
        }

        override fun onItemRangeRemoved(
            sender: ObservableArrayList<PlaneNoteViewData>?,
            positionStart: Int,
            itemCount: Int
        ) {

        }
    }


}
