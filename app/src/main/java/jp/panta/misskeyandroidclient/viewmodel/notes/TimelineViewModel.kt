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
    val requestBaseSetting: NoteRequest.Setting,
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

    private var isNoteCaptureStarted = false
    private var isTimelineCaptureStarted = false


    val position = MutableLiveData<Int>()

    private val notePagingStore = when(requestBaseSetting.type){
        NoteType.FAVORITE -> FavoriteNotePagingStore(connectionInstance, requestBaseSetting, misskeyAPI)
        else -> NoteTimelineStore(
            connectionInstance,
            requestBaseSetting,
            misskeyAPI
        )
    }
    private val timelineLiveData = object : TimelineLiveData(requestBaseSetting, notePagingStore, noteCapture, viewModelScope){
        override fun onActive() {
            super.onActive()

            if(settingStore.isAutoLoadTimeline && !settingStore.isAutoLoadTimelineWhenStopped){
                startTimelineCapture()
            }
            if(!settingStore.isCaptureNoteWhenStopped){
                startNoteCapture()
            }
        }

        override fun onInactive() {
            super.onInactive()

            if(settingStore.isAutoLoadTimeline && !settingStore.isAutoLoadTimelineWhenStopped){
                stopTimelineCapture()
            }
            if(!settingStore.isCaptureNoteWhenStopped){
                stopNoteCapture()
            }

        }
    }.apply{
        if(settingStore.isHideRemovedNote){
            noteCapture.addNoteRemoveListener(this.noteRemoveListener)
        }

    }

    private var mObserver = TimelineCapture.TimelineObserver.create(requestBaseSetting.type, timelineLiveData.timelineObserver)


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

    fun start(){
        startNoteCapture()
        startTimelineCapture()
        if(!streamingAdapter.isConnect){
            streamingAdapter.connect()
        }
    }

    fun stop(){
        stopNoteCapture()
        stopTimelineCapture()
    }


    private fun startTimelineCapture(){
        Log.d(tag, "タイムラインキャプチャーを開始しようとしている")
        val exTimeline = streamingAdapter.observerMap[timelineCaptureId]
        if(exTimeline == null && timelineCapture != null && !isTimelineCaptureStarted){
            //observer?.updateId()
            val observer = TimelineCapture.TimelineObserver.create(requestBaseSetting.type, timelineLiveData.timelineObserver)
            mObserver = observer
            streamingAdapter.addObserver(timelineCaptureId, timelineCapture)
            if(observer != null){
                timelineCapture.addChannelObserver(observer)
            }
        }
    }

    private fun stopTimelineCapture(){
        val exTimeline = streamingAdapter.observerMap[timelineCaptureId]
        if(exTimeline != null && timelineCapture != null){
            streamingAdapter.observerMap.remove(timelineCaptureId)
            val observer = mObserver
            if(observer != null){
                timelineCapture.removeChannelObserver(observer)
            }

        }
    }

    private fun startNoteCapture(){
        Log.d(tag, "ノートのキャプチャーを開始しようとしている")

        val exCapture = streamingAdapter.observerMap[noteCaptureId]
        if(exCapture == null && !isNoteCaptureStarted){
            Log.d("TimelineViewModel", "ノートのキャプチャーを開始した")

            isNoteCaptureStarted = true
            streamingAdapter.addObserver(noteCaptureId, noteCapture)
            val notes = timelineLiveData.value?.notes?: return
            noteCapture.addAll(notes)

        }
    }

    private fun stopNoteCapture(){
        val exCapture = streamingAdapter.observerMap[noteCaptureId]
        val notes = timelineLiveData.value?.notes
        if(exCapture != null && notes != null && isNoteCaptureStarted){
            noteCapture.removeAll(notes)
            streamingAdapter.observerMap.remove(noteCaptureId)
            isNoteCaptureStarted = false
        }
    }

}