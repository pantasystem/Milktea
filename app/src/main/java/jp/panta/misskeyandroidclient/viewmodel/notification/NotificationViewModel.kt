package jp.panta.misskeyandroidclient.viewmodel.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI,
    private val noteCapture: NoteCapture
) : ViewModel(){

    private var isLoadingFlag = false
    val isLoading = MutableLiveData<Boolean>()
    //loadNewはない

    val notificationsLiveData = MutableLiveData<List<NotificationViewData>>()

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
                    NotificationViewData((it))
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
                    NotificationViewData(it)
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

    fun addNoteObserver(notificationViewDataList: List<NotificationViewData>){
        val noteList = notificationViewDataList.asSequence().filter{
            it.noteViewData != null
        }.map{
            it.noteViewData!!
        }.toList()
        noteCapture.addAll(noteList)
    }


}