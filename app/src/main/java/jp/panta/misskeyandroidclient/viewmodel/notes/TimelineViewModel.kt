package jp.panta.misskeyandroidclient.viewmodel.notes


import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.TimelineState

class TimelineViewModel(
    private val connectionInstance: ConnectionInstance,
    private val requestBaseSetting: NoteRequest.Setting,
    private val noteCapture: NoteCapture,
    private val timelineCapture: TimelineCapture?
) : ViewModel(){

    val errorState = MediatorLiveData<String>()

    //private val connectionInstance = ConnectionInstance(instanceBaseUrl = baseUrl, userId = "7roinhytrr", userToken = "")

    private val timelineLiveData = TimelineLiveData(connectionInstance, requestBaseSetting, noteCapture, timelineCapture)

    val isLoading = timelineLiveData.isLoading

    val reNoteTarget = MutableLiveData<PlaneNoteViewData>()

    val replyTarget = MutableLiveData<PlaneNoteViewData>()

    val shareTarget = MutableLiveData<PlaneNoteViewData>()

    val targetUser = MutableLiveData<User>()

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

    fun setTargetToReNote(note: PlaneNoteViewData){
        reNoteTarget.postValue(note)
    }

    fun setTargetToReply(note: PlaneNoteViewData){
        replyTarget.postValue(note)
    }

    fun setTargetToShare(note: PlaneNoteViewData){
        shareTarget.postValue(note)
    }

    fun setTargetToUser(user: User){
        targetUser.postValue(user)
    }

}