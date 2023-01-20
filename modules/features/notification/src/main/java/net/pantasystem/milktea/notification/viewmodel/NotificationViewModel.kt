package net.pantasystem.milktea.notification.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.api_streaming.channel.ChannelAPI
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.notification.impl.NotificationCacheAdder
import net.pantasystem.milktea.data.streaming.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notification.GroupInvitedNotification
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationRelation
import net.pantasystem.milktea.model.notification.ReceiveFollowRequestNotification
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    loggerFactory: Logger.Factory,
    accountStore: AccountStore,
    private val noteTranslationStore: NoteTranslationStore,
    private val channelAPIAdapterProvider: ChannelAPIWithAccountProvider,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val notificationCacheAdder: NotificationCacheAdder,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    private val unreadNotificationDAO: UnreadNotificationDAO,
    private val groupRepository: GroupRepository,
    private val metaRepository: MetaRepository,
) : ViewModel() {


    val isLoading = MutableLiveData<Boolean>()
    private var isLoadingFlag = false
        set(value) {
            isLoading.postValue(value)
            field = value
        }

    private var noteCaptureScope = CoroutineScope(viewModelScope.coroutineContext + Dispatchers.IO)


    val notificationsLiveData = MutableLiveData<List<NotificationViewData>>()
    private val _error = MutableSharedFlow<Throwable>(
        onBufferOverflow = BufferOverflow.DROP_LATEST,
        extraBufferCapacity = 100
    )
    private var notifications: List<NotificationViewData> = emptyList()
        set(value) {
            notificationsLiveData.postValue(value)
            field = value
        }
    private val logger = loggerFactory.create("NotificationViewModel")

    init {
        accountStore.observeCurrentAccount.filterNotNull().flowOn(Dispatchers.IO)
            .onEach {
                loadInit()
            }.launchIn(viewModelScope)

        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
            channelAPIAdapterProvider.get(ac).connect(ChannelAPI.Type.Main).map {
                it as? ChannelBody.Main.Notification
            }.filterNotNull().map {
                ac to it
            }.map {
                it.first to notificationCacheAdder.addAndConvert(
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
                noteCaptureAPIAdapter,
                noteTranslationStore,
                metaRepository.get(account.normalizedInstanceDomain)?.emojis ?: emptyList()
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
        noteCaptureScope = CoroutineScope(viewModelScope.coroutineContext + Dispatchers.IO)

        logger.debug("before launch:${viewModelScope.isActive}")
        viewModelScope.launch(Dispatchers.IO) {
            logger.debug("in launch")
            val account = accountRepository.getCurrentAccount().getOrThrow()
            val request = NotificationRequest(i = account.token, limit = 20)
            val misskeyAPI = misskeyAPIProvider.get(account.normalizedInstanceDomain)

            runCancellableCatching {
                val notificationDTOList = misskeyAPI.notification(request).throwIfHasError().body()
                logger.debug("res: $notificationDTOList")
                val viewDataList = notificationDTOList?.toNotificationViewData(account)
                    ?: emptyList()
                viewDataList.forEach {
                    it.noteViewData?.eventFlow?.launchIn(noteCaptureScope)
                }
                unreadNotificationDAO.deleteWhereAccountId(account.accountId)

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
        if (exNotificationList.isEmpty() || untilId == null) {
            isLoadingFlag = false
            return loadInit()
        }

        viewModelScope.launch(Dispatchers.IO) {
            val account = accountRepository.getCurrentAccount().getOrThrow()
            val misskeyAPI = misskeyAPIProvider.get(account.normalizedInstanceDomain)

            val request = NotificationRequest(
                i = account.token,
                limit = 20,
                untilId = untilId.notificationId
            )
            val notifications = runCancellableCatching {
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
            viewModelScope.launch {
                runCancellableCatching {
                    userRepository.acceptFollowRequest(notification.userId)
                }.onSuccess {
                    loadInit()
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
                runCancellableCatching {
                    userRepository.rejectFollowRequest(notification.userId)
                }.onSuccess {
                    loadInit()
                }.onFailure {
                    logger.error("rejectFollowRequest error:$it")
                    _error.tryEmit(it)
                }
            }
        }
    }

    fun acceptGroupInvitation(notification: Notification) {
        viewModelScope.launch {
            if (notification is GroupInvitedNotification) {
                groupRepository.accept(notification.invitationId)
                    .onSuccess {
                        loadInit()
                    }
                    .onFailure {
                        logger.error("failed rejectGroupInvitation", it)
                        _error.tryEmit(it)
                    }
            }
        }

    }

    fun rejectGroupInvitation(notification: Notification) {
        viewModelScope.launch(Dispatchers.IO) {
            if (notification is GroupInvitedNotification) {
                groupRepository.reject(notification.invitationId)
                    .onSuccess {
                        loadInit()
                    }
                    .onFailure {
                        logger.error("failed rejectGroupInvitation", it)
                        _error.tryEmit(it)
                    }
            }

        }
    }

    private suspend fun NotificationDTO.toNotificationRelation(account: Account): NotificationRelation {
        return notificationCacheAdder.addAndConvert(account, this)
    }

    private suspend fun List<NotificationDTO>.toNotificationRelations(account: Account): List<NotificationRelation> {
        return this.mapNotNull {
            runCancellableCatching {
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
                noteCaptureAPIAdapter,
                noteTranslationStore,
                metaRepository.get(account.normalizedInstanceDomain)?.emojis ?: emptyList()
            )
        }
    }


}