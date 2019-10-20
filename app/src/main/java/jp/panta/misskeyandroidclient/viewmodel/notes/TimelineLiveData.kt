package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.viewmodel.TimelineState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimelineLiveData(
    private val connectionInstance: ConnectionInstance,
    private val requestBase: NoteRequest.Setting,
    private val noteCapture: NoteCapture,
    private val timelineCapture: TimelineCapture?
) : MutableLiveData<TimelineState>(){

    var isLoading =  MutableLiveData<Boolean>()

    private val misskeyAPI = MisskeyAPIServiceBuilder.build(connectionInstance.instanceBaseUrl)

    private val timelineStore = when(requestBase.type){
        NoteType.HOME -> misskeyAPI::homeTimeline
        NoteType.LOCAL -> misskeyAPI::localTimeline
        NoteType.SOCIAL -> misskeyAPI::hybridTimeline
        NoteType.GLOBAL -> misskeyAPI::globalTimeline
        NoteType.SEARCH -> misskeyAPI::searchNote
        NoteType.SEARCH_HASH -> misskeyAPI::searchByTag
        NoteType.USER -> misskeyAPI::userNotes

    }

    private var isLoadingFlag = false



    //private var timelineState: TimelineState? = null
    init{
        noteCapture.addNoteRemoveListener(object : NoteCapture.NoteRemoveListener{
            override fun onRemoved(id: String) {
                val list = value?.notes
                if(list == null){
                    return
                }else{
                    val timeline = ArrayList<PlaneNoteViewData>(list)
                    timeline.filter{
                        it.toShowNote.id == id
                    }.forEach{
                        timeline.remove(it)
                    }
                    postValue(TimelineState(timeline, TimelineState.State.REMOVED))
                }

            }
        })


    }

    override fun onActive() {
        super.onActive()

        if(timelineCapture != null){
            val observer = TimelineCapture.TimelineObserver.create(requestBase.type, timelineObserver)
            if(observer != null) timelineCapture.addChannelObserver(observer)

        }
    }


    fun loadInit(){
        this.isLoading.postValue(true)

        if( ! isLoadingFlag ){

            isLoadingFlag = true

            timelineStore(requestBase.buildRequest(NoteRequest.Conditions())).enqueue( object : Callback<List<Note>?>{
                override fun onResponse(call: Call<List<Note>?>, response: Response<List<Note>?>) {
                    val list = response.body()?.map{ it -> PlaneNoteViewData(it) }
                    if(list == null){
                        isLoadingFlag = false
                        return
                    }

                    //observableTimelineList.clear()
                    //observableTimelineList.addAll(list)
                    noteCapture.addAll(list)
                    val state = TimelineState(list, TimelineState.State.INIT)
                    postValue(state)
                    isLoadingFlag = false

                    //test()
                }

                override fun onFailure(call: Call<List<Note>?>, t: Throwable) {
                    isLoadingFlag = false
                }
            })
        }
    }

    fun loadNew(){
        if( ! isLoadingFlag ){
            isLoadingFlag = true
            //val sinceId = observableTimelineList.firstOrNull()?.id
            val sinceId = value?.getSinceId()
            if(sinceId == null){
                isLoadingFlag = false
                //初期化処理 or 何もしない
            }else{

                val req = requestBase.buildRequest(NoteRequest.Conditions(sinceId = sinceId))
                timelineStore(req).enqueue(object : Callback<List<Note>?> {
                    override fun onResponse(call: Call<List<Note>?>, response: Response<List<Note>?>) {
                        val newNotes = response.body()?.asReversed()
                        isLoadingFlag = false
                        val planeNotes = newNotes?.map{ it -> PlaneNoteViewData(it) }
                            ?: return

                        noteCapture.addAll(planeNotes)
                        //observableTimelineList.addAll(0, planeNotes)
                        var state = value
                        state = if(state == null){
                            TimelineState(planeNotes, TimelineState.State.LOAD_NEW)
                        }else{
                            val newList = ArrayList<PlaneNoteViewData>(state.notes).apply {
                                addAll(0, planeNotes)
                            }
                            TimelineState(newList, TimelineState.State.LOAD_NEW)
                        }
                        postValue(state)
                        isLoading.postValue(false)

                    }

                    override fun onFailure(call: Call<List<Note>?>, t: Throwable) {
                        isLoadingFlag = false
                        isLoading.postValue(false)
                    }
                })
            }

        }
    }

    fun loadOld(){
        val untilId = value?.getUntilId()
        if(  isLoadingFlag || untilId == null){
            //何もしない
        }else{
            isLoadingFlag = true
            val req = requestBase.buildRequest(NoteRequest.Conditions(untilId = untilId))
            timelineStore(req).enqueue(object : Callback<List<Note>?>{
                override fun onResponse(call: Call<List<Note>?>, response: Response<List<Note>?>) {
                    val list = response.body()?.map{ it -> PlaneNoteViewData(it) }

                    if(list == null){
                        isLoadingFlag = false
                        return
                    }

                    noteCapture.addAll(list)
                    //observableTimelineList.addAll(list)
                    var state = value
                    state = if(state == null){
                        TimelineState(list, TimelineState.State.LOAD_OLD)
                    }else{
                        val newList = ArrayList<PlaneNoteViewData>(state.notes).apply{
                            addAll(list)
                        }
                        TimelineState(newList, TimelineState.State.LOAD_OLD)
                    }
                    postValue(state)
                    isLoadingFlag = false

                }
                override fun onFailure(call: Call<List<Note>?>, t: Throwable) {
                    isLoadingFlag = false
                }
            })

        }
    }

    private val timelineObserver = object : TimelineCapture.Observer{
        override fun onReceived(note: PlaneNoteViewData) {
            noteCapture.add(note)
            val notes = value?.notes
            val list = if(notes == null){
                arrayListOf(note)
            }else{
                ArrayList<PlaneNoteViewData>(notes).apply{
                    add(0, note)
                }
            }
            postValue(TimelineState(list, TimelineState.State.RECEIVED_NEW))
        }
    }
}