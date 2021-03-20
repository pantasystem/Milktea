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
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthImpl
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.collections.ArrayList

@ExperimentalCoroutinesApi
class NotificationViewModel(
    private val miCore: MiCore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(){

    private val encryption: Encryption = miCore.getEncryption()


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

    init {
        miCore.getCurrentAccount().filterNotNull().onEach {
            loadInit()
        }.launchIn(viewModelScope + Dispatchers.IO)

        miCore.getCurrentAccount().filterNotNull().flatMapLatest { ac ->
            miCore.getChannelAPI(ac).connect(ChannelAPI.Type.MAIN).map {
                it as? ChannelBody.Main.Notification
            }.filterNotNull().map {
                ac to it
            }.map {
                it.first to miCore.getGetters().notificationRelationGetter.get(it.first, it.second.body)
            }
        }.map {  accountAndNotificationRelation ->
            val notificationRelation = accountAndNotificationRelation.second
            val account = accountAndNotificationRelation.first
            NotificationViewData(notificationRelation, account, DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter())
        }.onEach {

            val list = ArrayList(notifications)
            list.add(0, it)
            notifications = list
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun loadInit(){
        if(isLoadingFlag){
            return
        }
        isLoadingFlag = true
        noteCaptureScope.cancel()
        noteCaptureScope = CoroutineScope(viewModelScope.coroutineContext + ioDispatcher)

        viewModelScope.launch(ioDispatcher) {
            val account = miCore.getAccountRepository().getCurrentAccount()
            val request = NotificationRequest(i = account.getI(encryption), limit = 20)
            val misskeyAPI = miCore.getMisskeyAPIProvider().get(account.instanceDomain)

            val notificationDTOList = runCatching {
                misskeyAPI.notification(request).execute()?.body()
            }.getOrNull()
            val viewDataList = notificationDTOList?.toNotificationViewData(account)
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

        viewModelScope.launch(ioDispatcher) {
            val account = miCore.getAccountRepository().getCurrentAccount()
            val misskeyAPI = miCore.getMisskeyAPIProvider().get(account.instanceDomain)

            val request = NotificationRequest(i = account.getI(encryption), limit = 20, untilId = untilId.notificationId)
            val notifications = runCatching {
                misskeyAPI.notification(request).execute()?.body()
            }.getOrNull()
            val list = notifications?.toNotificationViewData(account)
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

    private suspend fun NotificationDTO.toNotificationRelation(account: Account): NotificationRelation {
        return miCore.getGetters().notificationRelationGetter.get(account, this)
    }

    private suspend fun List<NotificationDTO>.toNotificationRelations(account: Account): List<NotificationRelation> {
        return this.map {
            it.toNotificationRelation(account)
        }
    }

    private suspend fun List<NotificationDTO>.toNotificationViewData(account: Account): List<NotificationViewData> {
        return this.toNotificationRelations(account).map {
            NotificationViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter())
        }
    }


}