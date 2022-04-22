package jp.panta.misskeyandroidclient.ui.notification.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationRelation
import net.pantasystem.milktea.model.notification.ReceiveFollowRequestNotification
import net.pantasystem.milktea.data.streaming.ChannelBody
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.throwIfHasError
import kotlin.collections.ArrayList

@ExperimentalCoroutinesApi
class NotificationViewModel(
    private val miCore: MiCore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

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
    private val _error = MutableSharedFlow<Throwable>(
        onBufferOverflow = BufferOverflow.DROP_LATEST,
        extraBufferCapacity = 100
    )
    val error: Flow<Throwable> = _error
    private var notifications: List<NotificationViewData> = emptyList()
        set(value) {
            notificationsLiveData.postValue(value)
            field = value
        }
    private val logger = miCore.loggerFactory.create("NotificationViewModel")

    init {
        miCore.getAccountStore().observeCurrentAccount.filterNotNull().flowOn(Dispatchers.IO)
            .onEach {
                loadInit()
            }.launchIn(viewModelScope)

        miCore.getAccountStore().observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
            miCore.getChannelAPI(ac).connect(ChannelAPI.Type.Main).map {
                it as? ChannelBody.Main.Notification
            }.filterNotNull().map {
                ac to it
            }.map {
                it.first to miCore.getGetters().notificationRelationGetter.get(
                    it.first,
                    it.second.body
                )
            }
        }.map { accountAndNotificationRelation ->
            val notificationRelation = accountAndNotificationRelation.second
            val account = accountAndNotificationRelation.first
            NotificationViewData(
                notificationRelation,
                account,
                miCore.getNoteCaptureAdapter(),
                miCore.getTranslationStore()
            )
        }.catch { e ->
            logger.warning("ストーリミング受信中にエラー発生", e = e)
        }.onEach {

            it.noteViewData?.eventFlow?.launchIn(viewModelScope + Dispatchers.IO)
            val list = ArrayList(notifications)
            list.add(0, it)
            notifications = list
        }.launchIn(viewModelScope + Dispatchers.IO)

        //loadInit()
    }

    fun loadInit() {
        if (isLoadingFlag) {
            logger.debug("cancel loadInit")
            return
        }
        logger.debug("loadInit")

        isLoadingFlag = true
        //noteCaptureScope.cancel()
        noteCaptureScope = CoroutineScope(viewModelScope.coroutineContext + ioDispatcher)

        logger.debug("before launch:${viewModelScope.isActive}")
        viewModelScope.launch(Dispatchers.IO) {
            logger.debug("in launch")
            val account = miCore.getAccountRepository().getCurrentAccount()
            val request = NotificationRequest(i = account.getI(encryption), limit = 20)
            val misskeyAPI = miCore.getMisskeyAPIProvider().get(account.instanceDomain)

            runCatching {
                val notificationDTOList = misskeyAPI.notification(request).throwIfHasError().body()
                logger.debug("res: $notificationDTOList")
                val viewDataList = notificationDTOList?.toNotificationViewData(account)
                    ?: emptyList()
                viewDataList.forEach {
                    it.noteViewData?.eventFlow?.launchIn(noteCaptureScope)
                }
                miCore.getUnreadNotificationDAO().deleteWhereAccountId(account.accountId)

                viewDataList
            }.onSuccess {
                notifications = it
            }.onFailure {
                logger.error("読み込みエラー", e = it)
            }

            isLoadingFlag = false
        }

    }

    fun loadOld() {
        logger.debug("loadOld")
        if (isLoadingFlag) {
            return
        }
        isLoadingFlag = true

        val exNotificationList = notifications
        val untilId = exNotificationList.lastOrNull()?.id
        if (exNotificationList.isNullOrEmpty() || untilId == null) {
            isLoadingFlag = false
            return loadInit()
        }

        viewModelScope.launch(ioDispatcher) {
            val account = miCore.getAccountRepository().getCurrentAccount()
            val misskeyAPI = miCore.getMisskeyAPIProvider().get(account.instanceDomain)

            val request = NotificationRequest(
                i = account.getI(encryption),
                limit = 20,
                untilId = untilId.notificationId
            )
            val notifications = runCatching {
                misskeyAPI.notification(request).throwIfHasError().body()
            }.getOrNull()
            val list = notifications?.toNotificationViewData(account)
            if (list.isNullOrEmpty()) {
                isLoadingFlag = false
                return@launch
            }
            list.forEach {
                it.noteViewData?.eventFlow?.launchIn(noteCaptureScope)
            }

            val notificationViewDataList =
                ArrayList<NotificationViewData>(exNotificationList).also {
                    it.addAll(
                        list
                    )
                }
            this@NotificationViewModel.notifications = notificationViewDataList
            isLoadingFlag = false
        }


    }

    fun acceptFollowRequest(notification: Notification) {
        if (notification is ReceiveFollowRequestNotification) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    miCore.getUserRepository().acceptFollowRequest(notification.userId)
                }.onSuccess {
                    if (it) {
                        notifications = notifications.filterNot { n ->
                            n.id == notification.id
                        }
                    }
                }.onFailure {
                    logger.error("acceptFollowRequest error:$it")
                    _error.tryEmit(it)
                }
            }
        }

    }

    fun rejectFollowRequest(notification: Notification) {
        if (notification is ReceiveFollowRequestNotification) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    miCore.getUserRepository().rejectFollowRequest(notification.userId)
                }.onSuccess {
                    if (it) {
                        notifications = notifications.filterNot { n ->
                            n.id == notification.id
                        }
                    }
                }.onFailure {
                    logger.error("rejectFollowRequest error:$it")
                    _error.tryEmit(it)
                }
            }
        }
    }

    private suspend fun NotificationDTO.toNotificationRelation(account: Account): NotificationRelation {
        return miCore.getGetters().notificationRelationGetter.get(account, this)
    }

    private suspend fun List<NotificationDTO>.toNotificationRelations(account: Account): List<NotificationRelation> {
        return this.mapNotNull {
            runCatching {
                it.toNotificationRelation(account)
            }.onFailure {
                logger.error("変換失敗", e = it)
            }.getOrNull()
        }
    }

    private suspend fun List<NotificationDTO>.toNotificationViewData(account: Account): List<NotificationViewData> {
        return this.toNotificationRelations(account).map {
            NotificationViewData(
                it,
                account,
                miCore.getNoteCaptureAdapter(),
                miCore.getTranslationStore()
            )
        }
    }


}