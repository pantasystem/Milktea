package jp.panta.misskeyandroidclient.viewmodel.notification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.Event
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureEvent
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationRequest
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import jp.panta.misskeyandroidclient.model.streming.note.v2.NoteCapture
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class NotificationViewModel(
    private val account: Account,
    private val misskeyAPI: MisskeyAPI,
    private val miCore: MiCore,
    private val encryption: Encryption = miCore.getEncryption()
    //private val noteCapture: NoteCapture
) : ViewModel(){



    private var isLoadingFlag = false
    val isLoading = MutableLiveData<Boolean>()
    //loadNewはない


    // private val streamingAdapter = StreamingAdapter(accountRelation.getCurrentConnectionInformation(), encryption)
    private val noteCapture = miCore.getNoteCapture(account)

    private val noteCaptureClient = NoteCapture.Client()

    var mEventStoreStreamDisposable: Disposable? = null

    val notificationsLiveData = object : MutableLiveData<List<NotificationViewData>>(){
        override fun onActive() {
            super.onActive()
            noteCapture.attachClient(noteCaptureClient)
            if(mEventStoreStreamDisposable == null){
                mEventStoreStreamDisposable = miCore.getNoteEventStore(account).getEventStream().subscribe {
                    noteEventObserver(it)
                }
            }


        }
        override fun onInactive() {
            super.onInactive()
            //noteCapture.detach(noteRegister)
            noteCapture.detachClient(noteCaptureClient)
            mEventStoreStreamDisposable?.dispose()
        }

    }

    fun loadInit(){
        if(isLoadingFlag){
            return
        }
        isLoadingFlag = true
        val request = NotificationRequest(i = account.getI(encryption), limit = 20)
        misskeyAPI.notification(request).enqueue(object : Callback<List<Notification>?>{
            override fun onResponse(
                call: Call<List<Notification>?>,
                response: Response<List<Notification>?>
            ) {
                val list = try{
                    response.body()?.mapNotNull{
                        try{
                            NotificationViewData((it), account, DetermineTextLengthSettingStore(miCore.getSettingStore()))

                        }catch(e: Exception){
                            Log.e("NotificationViewModel", "error:${it}", e)
                            null
                        }
                    }
                }catch(e: Exception){
                    Log.e("NotificationViewModel", "error:${response.body()}", e)
                    null
                }
                notificationsLiveData.postValue(
                    list
                )
                if(list != null){
                    noteCaptureClient.unCaptureAll()
                    noteCapture.attachClient(noteCaptureClient)
                    noteCaptureClient.captureAll(
                        list.pickNoteIds()
                    )

                }

                isLoadingFlag = false
                isLoading.postValue(false)
                miCore.notificationSubscribeViewModel.readAllNotifications(account)
            }
            override fun onFailure(call: Call<List<Notification>?>, t: Throwable) {
                isLoadingFlag = false
                isLoading.postValue(false)
            }
        })
    }
    fun loadOld(){
        if(isLoadingFlag){
            return
        }
        isLoadingFlag = true

        val exNotificationList = notificationsLiveData.value
        val untilId = exNotificationList?.lastOrNull()?.id
        if(exNotificationList == null || untilId == null){
            isLoadingFlag = false
            return
        }

        val request = NotificationRequest(i = account.getI(encryption), limit = 20, untilId = untilId)
        misskeyAPI.notification(request).enqueue(object : Callback<List<Notification>?>{
            override fun onResponse(
                call: Call<List<Notification>?>,
                response: Response<List<Notification>?>
            ) {
                val rawList = response.body()
                if(rawList == null){
                    isLoadingFlag = false
                    return
                }

                val list = rawList.map{
                    NotificationViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore()))
                }

                val notificationViewDataList = ArrayList<NotificationViewData>(exNotificationList).apply{
                    addAll(
                        list
                    )
                }

                noteCaptureClient.captureAll(list.pickNoteIds())


                notificationsLiveData.postValue(notificationViewDataList)
                isLoadingFlag = false
            }

            override fun onFailure(call: Call<List<Notification>?>, t: Throwable) {
                isLoadingFlag = false
            }
        })
    }


    private fun List<NotificationViewData>.pickNoteIds(): List<String>{
        val hashSet = HashSet<String>()
        for(notify in this){
            if(notify.noteViewData != null){
                hashSet.add(notify.noteViewData.id)
                hashSet.add(notify.noteViewData.toShowNote.id)
            }
        }
        return hashSet.toList()
    }
    private fun noteEventObserver(noteEvent: NoteCaptureEvent){
        Log.d("TM-VM", "#noteEventObserver $noteEvent")
        val timelineNotes = notificationsLiveData.value
            ?: return


        val updatedNotification = when(noteEvent.event){

            is Event.Deleted ->{
                timelineNotes.filterNot{ notification ->
                    notification.noteViewData?.id == noteEvent.noteId || notification.noteViewData?.toShowNote?.id == noteEvent.noteId
                }
            }
            else -> timelineNotes.map{
                val note: PlaneNoteViewData? = it.noteViewData
                if(note?.toShowNote?.id == noteEvent.noteId){
                    when(noteEvent.event){
                        is Event.NewNote.Reacted ->{
                            note.addReaction(noteEvent.event.reaction, noteEvent.event.emoji, noteEvent.event.userId == account.remoteId)
                        }
                        is Event.NewNote.UnReacted ->{
                            note.takeReaction(noteEvent.event.reaction, noteEvent.event.userId == account.remoteId)
                        }
                        is Event.NewNote.Voted ->{
                            note.poll?.update(noteEvent.event.choice, noteEvent.event.userId == account.remoteId)
                        }

                    }
                }

                it
            }
        }
        if(noteEvent.event is Event.Deleted){
            notificationsLiveData.postValue(updatedNotification)
        }
    }

}