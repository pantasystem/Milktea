package jp.panta.misskeyandroidclient.viewmodel.notification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.streming.note.NoteRegister
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class NotificationViewModel(
    private val accountRelation: AccountRelation,
    private val misskeyAPI: MisskeyAPI,
    private val miCore: MiCore,
    private val encryption: Encryption = miCore.getEncryption()
    //private val noteCapture: NoteCapture
) : ViewModel(){
    private val connectionInstance = accountRelation.getCurrentConnectionInformation()



    private var isLoadingFlag = false
    val isLoading = MutableLiveData<Boolean>()
    //loadNewはない


   // private val streamingAdapter = StreamingAdapter(accountRelation.getCurrentConnectionInformation(), encryption)
    private val noteCapture = miCore.getNoteCapture(accountRelation)

    private var noteRegister = NoteRegister()

    val noteCaptureId = UUID.randomUUID().toString()

    val notificationsLiveData = object : MutableLiveData<List<NotificationViewData>>(){
        override fun onActive() {
            super.onActive()
            //streamingAdapter.addObserver(noteCaptureId, noteCapture)
            noteCapture.attach(noteRegister)

        }
        override fun onInactive() {
            super.onInactive()
            noteCapture.detach(noteRegister)
        }

    }

    fun loadInit(){
        if(isLoadingFlag){
            return
        }
        isLoadingFlag = true
        val request = NotificationRequest(i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!, limit = 20)
        misskeyAPI.notification(request).enqueue(object : Callback<List<Notification>?>{
            override fun onResponse(
                call: Call<List<Notification>?>,
                response: Response<List<Notification>?>
            ) {
                val list = response.body()?.map{
                    NotificationViewData((it), accountRelation.account)
                }
                notificationsLiveData.postValue(
                    list
                )
                if(list != null){
                    noteCapture.detach(noteRegister)
                    noteRegister = NoteRegister()
                    noteCapture.attach(noteRegister)
                    noteCapture.subscribeAll(noteRegister.registerId, list.mapNotNull{
                        it.noteViewData
                    })
                }

                isLoadingFlag = false
                isLoading.postValue(false)
                miCore.notificationSubscribeViewModel.readAllNotifications(accountRelation)
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

        val request = NotificationRequest(i = connectionInstance?.getI(encryption)!!, limit = 20, untilId = untilId)
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
                    NotificationViewData(it, accountRelation.account)
                }

                val notificationViewDataList = ArrayList<NotificationViewData>(exNotificationList).apply{
                    addAll(
                        list
                    )
                }

                noteCapture.subscribeAll(noteRegister.registerId, list.mapNotNull {
                    it.noteViewData
                })

                notificationsLiveData.postValue(notificationViewDataList)
                isLoadingFlag = false
            }

            override fun onFailure(call: Call<List<Notification>?>, t: Throwable) {
                isLoadingFlag = false
            }
        })
    }



}