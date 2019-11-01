package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class TimelineLiveData(
    private val requestBase: NoteRequest.Setting,
    private val notePagedStore: NotePagedStore,
    private val noteCapture: NoteCapture,
    private val timelineCapture: TimelineCapture?,
    private val coroutineScope: CoroutineScope
) : MutableLiveData<TimelineState>(){

    var isLoading =  MutableLiveData<Boolean>()

    private var isLoadingFlag = false

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
                    postValue(
                        TimelineState(
                            timeline,
                            TimelineState.State.REMOVED
                        )
                    )
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
        //val notes = value?.notes?: return
        //noteCapture.addAll(notes)
    }


    fun loadInit(){
        this.isLoading.postValue(true)

        if( ! isLoadingFlag ){

            isLoadingFlag = true

            coroutineScope.launch(Dispatchers.IO){
                try{
                    val response = notePagedStore.loadInit()
                    val list = response.second
                    if(list == null || list.isEmpty()){
                        isLoadingFlag = false
                        isLoading.postValue(false)
                        return@launch
                    }else{
                        val state = TimelineState(
                            list,
                            TimelineState.State.INIT
                        )
                        postValue(state)
                        noteCapture.addAll(list)
                        isLoadingFlag = false

                    }

                }catch(e: IOException){
                    isLoadingFlag = false
                    isLoading.postValue(false)
                }

            }

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
                isLoading.postValue(false)
                return
            }
            coroutineScope.launch(Dispatchers.IO){
                try{
                    val res = notePagedStore.loadNew(sinceId)
                    val list = res.second
                    if(list == null || list.isEmpty()){
                        isLoadingFlag = false
                        isLoading.postValue(false)
                        return@launch
                    }else{
                        noteCapture.addAll(list)

                        val state = value
                        val newState = if(state == null){
                            TimelineState(
                                list,
                                TimelineState.State.LOAD_NEW
                            )
                        }else{
                            val newList = ArrayList<PlaneNoteViewData>(state.notes).apply {
                                addAll(0, list)
                            }
                            TimelineState(
                                newList,
                                TimelineState.State.LOAD_NEW
                            )
                        }
                        postValue(newState)
                        isLoadingFlag = false
                        isLoading.postValue(false)
                    }
                }catch(e: IOException){
                    isLoadingFlag = false
                    isLoading.postValue(false)
                }

            }

        }
    }

    fun loadOld(){
        val untilId = value?.getUntilId()
        if( isLoadingFlag || untilId == null){
            return
        }
        isLoadingFlag = true
        coroutineScope.launch(Dispatchers.IO){
            try {
                val res = notePagedStore.loadOld(untilId)
                val list = res.second
                if(list == null || list.isEmpty()){
                    isLoadingFlag = false
                    return@launch
                }else{
                    noteCapture.addAll(list)
                    val state = value

                    val newState = if(state == null){
                        TimelineState(
                            list,
                            TimelineState.State.LOAD_OLD
                        )
                    }else{
                        val newList = ArrayList<PlaneNoteViewData>(state.notes).apply{
                            addAll(list)
                        }
                        TimelineState(
                            newList,
                            TimelineState.State.LOAD_OLD
                        )
                    }
                    postValue(newState)
                    isLoadingFlag = false
                }
            }catch (e: IOException){
                isLoadingFlag = false
            }
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
            postValue(
                TimelineState(
                    list,
                    TimelineState.State.RECEIVED_NEW
                )
            )
        }
    }
}