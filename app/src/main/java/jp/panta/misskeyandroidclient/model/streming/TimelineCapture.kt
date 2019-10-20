package jp.panta.misskeyandroidclient.model.streming

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.Expose
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import java.util.*
import kotlin.collections.HashMap

class TimelineCapture : Observer{

    interface Observer{
        fun onReceived(note: PlaneNoteViewData)
    }
    class TimelineObserver(
        override val type: String = "connect",
        val body: RequestBody,
        @Expose
        val observer : Observer
    ) : StreamingAction{
        companion object{
            fun create(channel: NoteType, observer: Observer): TimelineObserver?{
                val timelineType = when(channel){
                    NoteType.HOME -> "homeTimeline"
                    NoteType.SOCIAL -> "hybridTimeline"
                    NoteType.LOCAL -> "localTimeline"
                    NoteType.GLOBAL -> "globalTimeline"
                    else -> null
                }
                timelineType?: return null
                return TimelineObserver(body = RequestBody(id = UUID.randomUUID().toString(), channel = timelineType), observer = observer)
            }
        }
    }

    data class RequestBody(
        val id: String,
        val channel: String
    )

    private data class Response(override val type: String, val body: Body): StreamingAction
    private data class Body(val id: String, val type: String, val body: Note?)
    /*
    main
    {
      type: "channel",
      body: { id: "72943",
              type: "readAllNotifications,
              body: null
             }
     }

     */

    /*
    timeline
    {
      type: "channel",
        body: {
          id: "23948203",
          type: "note",
          body: Note
     }
     */

    override var streamingAdapter: StreamingAdapter? = null

    private val observerMap = HashMap<String, TimelineObserver>()

    private val gson = Gson()

    override fun onConnect() {
        observerMap.forEach{
            streamingAdapter?.send(gson.toJson(it.value))
        }
    }

    override fun onDissconnect() {
    }

    override fun onReceived(msg: String) {
        try{
            val res = gson.fromJson(msg, Response::class.java)
            Log.d("TimelineCapture", "onReceived: $msg")
            val note = res.body.body
            val id = res.body.id
            if(note != null){
                observerMap[id]?.observer?.onReceived(PlaneNoteViewData(note))
            }
        }catch(e: JsonSyntaxException){
            Log.d("TimelineCapture", "遺物排除")
        }

    }

    fun addChannelObserver(observer: TimelineObserver){
        observerMap[observer.body.id] = observer
        Log.d("TimelineCapture", "登録しました: ${gson.toJson(observer)}")
        streamingAdapter?.send(gson.toJson(observer))
    }
}