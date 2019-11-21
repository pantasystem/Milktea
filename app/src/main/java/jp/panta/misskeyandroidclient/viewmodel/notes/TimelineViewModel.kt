package jp.panta.misskeyandroidclient.viewmodel.notes


import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.viewmodel.notes.favorite.FavoriteNotePagingStore
import java.lang.IndexOutOfBoundsException
import java.util.*

class TimelineViewModel(
    val connectionInstance: ConnectionInstance,
    requestBaseSetting: NoteRequest.Setting,
    misskeyAPI: MisskeyAPI,
    private val settingStore: SettingStore
) : ViewModel(){
    val tag = "TimelineViewModel"
    val errorState = MediatorLiveData<String>()

    private val streamingAdapter = StreamingAdapter(connectionInstance)
    private val noteCapture = NoteCapture(connectionInstance.userId)
    private val timelineCapture = if(settingStore.isAutoLoadTimeline){
        TimelineCapture(connectionInstance)
    }else{
        null
    }


    val position = MutableLiveData<Int>()

    private val notePagingStore = when(requestBaseSetting.type){
        NoteType.FAVORITE -> FavoriteNotePagingStore(connectionInstance, requestBaseSetting, misskeyAPI)
        else -> NoteTimelineStore(
            connectionInstance,
            requestBaseSetting,
            misskeyAPI
        )
    }
    private val timelineLiveData = TimelineLiveData(requestBaseSetting, notePagingStore, noteCapture, timelineCapture, viewModelScope)

    val observer = TimelineCapture.TimelineObserver.create(requestBaseSetting.type, timelineLiveData.timelineObserver)
    init{
        if(observer != null){
            timelineCapture?.addChannelObserver(observer)
        }
    }

    private val noteCaptureId = UUID.randomUUID().toString()
    private val timelineCaptureId = UUID.randomUUID().toString()

    val isLoading = timelineLiveData.isLoading



    fun getTimelineLiveData() : LiveData<TimelineState>{
        return timelineLiveData
    }

    fun loadNew(){
        timelineLiveData.loadNew()
    }

    fun loadOld(){
        timelineLiveData.loadOld()
    }

    fun loadInit(){
        timelineLiveData.loadInit()
    }

    fun streamingStart(){
        if( !streamingAdapter.isConnect){
            streamingAdapter.connect()

        }
        /*val hasNoteCapture = streamingAdapter.observers.any {
            it.javaClass.name == noteCapture.javaClass.name
        }*/
        val hasNoteCapture = streamingAdapter.observerMap[noteCaptureId] != null

        if(!hasNoteCapture){

            val notes = timelineLiveData.value?.notes
            if(notes != null){
                noteCapture.addAll(notes)
            }
            streamingAdapter.addObserver(noteCaptureId, noteCapture)
            Log.d(tag, "NoteCaptureを追加した")
        }

        if(settingStore.isAutoLoadTimeline && timelineCapture != null){
            /*val hasTimelineCapture = streamingAdapter.observers.any{
                it.javaClass.name == timelineCapture.javaClass.name
            }*/
            val hasTimelineCapture = streamingAdapter.observerMap[timelineCaptureId] != null
            if(!hasTimelineCapture){
                streamingAdapter.addObserver(timelineCaptureId, timelineCapture)
            }

        }



    }

    fun streamingStop(){
        if(!settingStore.isCaptureNoteWhenStopped){
            val notes = timelineLiveData.value?.notes
            if(notes != null){
                noteCapture.removeAll(notes)
            }
            /*val index = streamingAdapter.observers.indexOfFirst {
                it.javaClass.name == noteCapture.javaClass.name
            }*/
            try{
                //streamingAdapter.observers.removeAt(index)
                streamingAdapter.observerMap.remove(noteCaptureId)
                Log.d(tag, "NoteCaptureを削除した")
            }catch(e: IndexOutOfBoundsException){

            }


        }
        if(settingStore.isAutoLoadTimeline && !settingStore.isAutoLoadTimelineWhenStopped && timelineCapture != null){
            /*val index = streamingAdapter.observers.indexOfFirst {
                it.javaClass.name == timelineCapture.javaClass.name
            }*/
            try{
                //streamingAdapter.observers.removeAt(index)
                streamingAdapter.observerMap[timelineCaptureId]
            }catch(e: IndexOutOfBoundsException){

            }
        }


    }


}