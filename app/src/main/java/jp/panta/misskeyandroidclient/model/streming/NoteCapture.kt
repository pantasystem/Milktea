package jp.panta.misskeyandroidclient.model.streming

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import java.lang.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NoteCapture(
    override val account: Account,
    var myUserId: String?
) : AbsObserver(){


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

    data class CaptureRequest(override val type: String = "sn", val body: CaptureRequestBody): StreamingAction
    data class CaptureRequestBody(private val id: String)

    data class NoteUpdated(override val type: String, val body: Body<NoteUpdatedBody>): StreamingAction
    data class NoteUpdatedBody(val reaction: String?, val userId: String?, val choice: Int?, val deletedAt: String?, val emoji: Emoji?)

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

    override fun onClosing() {
        val notes = ArrayList<PlaneNoteViewData>()
        synchronized(observeNoteMap){
            observeNoteMap.forEach {
                val event = it.value
                notes.addAll(event.notes)
            }
        }
        removeAll(notes)
    }
    override fun onDisconnect() = Unit

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
                receivedObject.body.type == "reacted" -> addReaction(id, reaction!!, receivedObject.body.body.emoji, isMyReaction)
                receivedObject.body.type == "unreacted" -> removeReaction(id, reaction!!, isMyReaction)
                receivedObject.body.type == "pollVoted" -> updatePoll(id, receivedObject.body.body?.choice!!, isMyReaction)
                //else -> Log.d("NoteCapture", "不明なイベント")
            }

            //Log.d("NoteCapture", "onReceived: $receivedObject")
        }catch(e: JsonSyntaxException){
            //他のイベントが流れてくるので回避する
        }catch(e: Exception){

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
                true

            }

        }

    }

    fun remove(planeNoteViewData: PlaneNoteViewData){
        val key = planeNoteViewData.toShowNote.id
        synchronized(observeNoteMap){
            val noteEvents = observeNoteMap[key]
                ?: return
            synchronized(noteEvents){
                val iterator = noteEvents.notes.iterator()
                while(iterator.hasNext()){
                    val next = iterator.next()
                    if(next === planeNoteViewData){
                        iterator.remove()
                    }
                }
                if(noteEvents.notes.size < 1){
                    observeNoteMap.remove(key)
                    streamingAdapter?.send(gson.toJson(createUnCaptureRequest(key)))
                }
            }

        }
    }

    fun addAll(planeNoteViewDataList: List<PlaneNoteViewData>){
        planeNoteViewDataList.forEach{
            add(it)
        }
    }

    fun removeAll(planeNoteViewDataList: List<PlaneNoteViewData>){
        planeNoteViewDataList.forEach{
            remove(it)
        }
    }

    fun addNoteRemoveListener(listener: NoteRemoveListener){
        noteRemovedListeners.add(listener)
    }

    private fun createCaptureRequest(noteId: String): CaptureRequest{
        return CaptureRequest(body = CaptureRequestBody(noteId))
    }

    private fun createUnCaptureRequest(noteId: String): CaptureRequest{
        return CaptureRequest(type = "unsubNote", body = CaptureRequestBody(noteId))
    }

    private fun addReaction(noteId: String, reaction: String, emoji: Emoji?, isMyReaction: Boolean){
        synchronized(observeNoteMap){
            observeNoteMap[noteId]?.notes?.forEach {
                it.addReaction(reaction, emoji, isMyReaction)
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

    private fun updatePoll(noteId: String, number: Int, isMyReaction: Boolean){
        synchronized(observeNoteMap){
            observeNoteMap[noteId]?.notes?.forEach{
                it.poll?.update(number, isMyReaction)
            }
        }
    }


}
