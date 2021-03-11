package jp.panta.misskeyandroidclient.viewmodel.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.notification.NotificationDTO
import jp.panta.misskeyandroidclient.api.notification.NotificationRequest
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notification.NotificationRelation
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import java.util.*
import kotlin.collections.ArrayList

class NotificationViewModel(
    private val account: Account,
    private val misskeyAPI: MisskeyAPI,
    private val miCore: MiCore,
    private val encryption: Encryption = miCore.getEncryption(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    //private val noteCapture: NoteCapture
) : ViewModel(){



    val isLoading = MutableLiveData<Boolean>()
    private var isLoadingFlag = false
        set(value) {
            isLoading.postValue(value)
            field = value
        }
    //loadNewはない

    private var noteCaptureScope = CoroutineScope(viewModelScope.coroutineContext + ioDispatcher)

    // private val streamingAdapter = StreamingAdapter(accountRelation.getCurrentConnectionInformation(), encryption)


    val notificationsLiveData = MutableLiveData<List<NotificationViewData>>()
    private var notifications: List<NotificationViewData> = emptyList()
        set(value) {
            notificationsLiveData.postValue(value)
            field = value
        }

    fun loadInit(){
        if(isLoadingFlag){
            return
        }
        isLoadingFlag = true
        noteCaptureScope.cancel()
        noteCaptureScope = CoroutineScope(viewModelScope.coroutineContext + ioDispatcher)

        val request = NotificationRequest(i = account.getI(encryption), limit = 20)
        viewModelScope.launch(ioDispatcher) {
            val notificationDTOList = runCatching {
                misskeyAPI.notification(request).execute()?.body()
            }.getOrNull()
            val viewDataList = notificationDTOList?.toNotificationViewData()
                ?: emptyList()
            viewDataList.forEach {
                it.noteViewData?.eventFlow?.launchIn(noteCaptureScope)
            }
            notifications = viewDataList
            isLoadingFlag = false
        }

    }
    fun loadOld(){
        if(isLoadingFlag){
            return
        }
        isLoadingFlag = true

        val exNotificationList = notifications
        val untilId = exNotificationList.lastOrNull()?.id
        if(exNotificationList.isNullOrEmpty() || untilId == null){
            isLoadingFlag = false
            return
        }

        val request = NotificationRequest(i = account.getI(encryption), limit = 20, untilId = untilId.notificationId)
        viewModelScope.launch(ioDispatcher) {
            val notifications = runCatching {
                misskeyAPI.notification(request).execute()?.body()
            }.getOrNull()
            val list = notifications?.toNotificationViewData()
            if(list.isNullOrEmpty()) {
                isLoadingFlag = false
                return@launch
            }
            list.forEach {
                it.noteViewData?.eventFlow?.launchIn(noteCaptureScope)
            }

            val notificationViewDataList = ArrayList<NotificationViewData>(exNotificationList).also {
                it.addAll(
                    list
                )
            }
            this@NotificationViewModel.notifications = notificationViewDataList
            isLoadingFlag = false
        }


    }

    private suspend fun NotificationDTO.toNotificationRelation(): NotificationRelation {
        return miCore.getGetters().notificationRelationGetter.get(account, this)
    }

    private suspend fun List<NotificationDTO>.toNotificationRelations(): List<NotificationRelation> {
        return this.map {
            it.toNotificationRelation()
        }
    }

    private suspend fun List<NotificationDTO>.toNotificationViewData(): List<NotificationViewData> {
        return this.toNotificationRelations().map {
            NotificationViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter())
        }
    }


}