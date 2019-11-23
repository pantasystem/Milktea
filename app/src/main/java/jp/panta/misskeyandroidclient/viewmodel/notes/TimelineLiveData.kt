package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

open class TimelineLiveData(
    private val requestBase: NoteRequest.Setting,
    private val notePagedStore: NotePagedStore,
    private val noteCapture: NoteCapture,
    private val coroutineScope: CoroutineScope
) : MutableLiveData<TimelineState>(){

    var isLoading =  MutableLiveData<Boolean>()

    private var isLoadingFlag = false



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
                }catch (e: Exception){
                    Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                }

            }

        }
    }

    fun loadNew(){
        Log.d("TimelineLiveData", "loadNew")
        if( ! isLoadingFlag ){
            isLoadingFlag = true
            //val sinceId = observableTimelineList.firstOrNull()?.id
            val sinceId = value?.getSinceId()
            if(sinceId == null){
                isLoadingFlag = false
                isLoading.postValue(false)
                return loadInit()
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
                }catch (e: Exception){
                    Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                }

            }

        }
    }

    fun loadOld(){
        val untilId = value?.getUntilId() ?: return loadInit()
        if( isLoadingFlag){
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
            }catch(e: Exception){
                Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
            }
        }



    }
    val noteRemoveListener = object : NoteCapture.NoteRemoveListener{
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
    }

    val timelineObserver = object : TimelineCapture.Observer{
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