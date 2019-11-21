package jp.panta.misskeyandroidclient.viewmodel.notification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class NotificationViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI
    //private val noteCapture: NoteCapture
) : ViewModel(){

    private var isLoadingFlag = false
    val isLoading = MutableLiveData<Boolean>()
    //loadNewはない

    private val streamingAdapter = StreamingAdapter(connectionInstance)
    private val noteCapture = NoteCapture(connectionInstance.userId)

    val noteCaptureId = UUID.randomUUID().toString()

    val notificationsLiveData = object : MutableLiveData<List<NotificationViewData>>(){
        override fun onActive() {
            super.onActive()
            streamingAdapter.addObserver(noteCaptureId, noteCapture)
            val list = value
            if(list != null){
                addNoteObserver(list)
            }
            streamingAdapter.connect()

        }
        override fun onInactive() {
            super.onInactive()
            val list = value
            if(list != null){
                removeNoteObserver(list)
            }
            //streamingAdapter.observers.clear()
            streamingAdapter.observerMap.clear()

            streamingAdapter.disconnect()
        }

    }

    fun loadInit(){
        if(isLoadingFlag){
            return
        }
        isLoadingFlag = true
        val request = NotificationRequest(i = connectionInstance.getI()!!, limit = 20)
        misskeyAPI.notification(request).enqueue(object : Callback<List<Notification>?>{
            override fun onResponse(
                call: Call<List<Notification>?>,
                response: Response<List<Notification>?>
            ) {
                val list = response.body()?.map{
                    NotificationViewData((it), connectionInstance)
                }
                notificationsLiveData.postValue(
                    list
                )
                if(list != null){
                    addNoteObserver(list)
                }

                isLoadingFlag = false
                isLoading.postValue(false)
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

        val request = NotificationRequest(i = connectionInstance.getI()!!, limit = 20, untilId = untilId)
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
                    NotificationViewData(it, connectionInstance)
                }

                val notificationViewDataList = ArrayList<NotificationViewData>(exNotificationList).apply{
                    addAll(
                        list
                    )
                }

                addNoteObserver(list)

                notificationsLiveData.postValue(notificationViewDataList)
                isLoadingFlag = false
            }

            override fun onFailure(call: Call<List<Notification>?>, t: Throwable) {
                isLoadingFlag = false
            }
        })
    }

    private fun removeNoteObserver(notificationViewDataList: List<NotificationViewData>){
        val notes = notificationViewDataList.asSequence().filter{
            it.noteViewData != null
        }.map{
            it.noteViewData!!
        }.toList()
        noteCapture.removeAll(notes)
    }

    fun addNoteObserver(notificationViewDataList: List<NotificationViewData>){

        notificationViewDataList.asSequence().filter{
            //ノートが含まれない投稿を排除する
            it.noteViewData != null
        }.map{
            it.noteViewData!!
        }.toList().let{
            //同様のノートの場合キャプチャーを一度解除する
            noteCapture.removeAll(it)

            noteCapture.addAll(it)
        }

    }



}