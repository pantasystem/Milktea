package jp.panta.misskeyandroidclient.viewmodel.notes


import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.viewmodel.TimelineState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimelineViewModel(
    private val connectionInstance: ConnectionInstance,
    private val requestBaseSetting: NoteRequest.Setting,
    private val noteCapture: NoteCapture,
    private val timelineCapture: TimelineCapture?
) : ViewModel(){


    //val observableTimelineList: ObservableArrayList<PlaneNoteViewData> = ObservableArrayList()


    val errorState = MediatorLiveData<String>()

    private val baseUrl = "https://misskey.io/"

    private val misskeyAPI = MisskeyAPIServiceBuilder.build(baseUrl)

    //private val connectionInstance = ConnectionInstance(instanceBaseUrl = baseUrl, userId = "7roinhytrr", userToken = "")

    private val timelineLiveData = TimelineLiveData(connectionInstance, requestBaseSetting, noteCapture, timelineCapture)

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