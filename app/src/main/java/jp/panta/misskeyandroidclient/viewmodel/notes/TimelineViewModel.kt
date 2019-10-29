package jp.panta.misskeyandroidclient.viewmodel.notes


import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.viewmodel.notes.favorite.FavoriteNotePagingStore

class TimelineViewModel(
    connectionInstance: ConnectionInstance,
    requestBaseSetting: NoteRequest.Setting,
    misskeyAPI: MisskeyAPI,
    noteCapture: NoteCapture,
    timelineCapture: TimelineCapture?
) : ViewModel(){

    val errorState = MediatorLiveData<String>()

    //private val connectionInstance = ConnectionInstance(instanceBaseUrl = baseUrl, userId = "7roinhytrr", userToken = "")

    /*private val notePagingStore = NoteTimelineStore(
        connectionInstance,
        requestBaseSetting,
        misskeyAPI
    )*/

    private val notePagingStore = when(requestBaseSetting.type){
        NoteType.FAVORITE -> FavoriteNotePagingStore(connectionInstance, requestBaseSetting, misskeyAPI)
        else -> NoteTimelineStore(
            connectionInstance,
            requestBaseSetting,
            misskeyAPI
        )
    }
    private val timelineLiveData = TimelineLiveData(requestBaseSetting, notePagingStore, noteCapture, timelineCapture, viewModelScope)

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



}