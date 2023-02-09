package net.pantasystem.milktea.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.common_android_ui.APIErrorStringConverter
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.notification.*
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val notificationStreaming: NotificationStreaming,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    loggerFactory: Logger.Factory,
    accountStore: AccountStore,
    notificationPagingStoreFactory: NotificationPagingStore.Factory
) : ViewModel() {

    private val planeNoteViewDataCache: PlaneNoteViewDataCache =
        planeNoteViewDataCacheFactory.create({
            accountRepository.getCurrentAccount().getOrThrow()
        }, viewModelScope)

    private val notificationPagingStore = notificationPagingStoreFactory.create {
        accountRepository.getCurrentAccount().getOrThrow()
    }

    private val notificationPageableState = notificationPagingStore.notifications.map { state ->
        state.suspendConvert { list ->
            list.map { n ->
                val noteViewData = n.note?.let {
                    planeNoteViewDataCache.get(it)
                }
                NotificationViewData(
                    n,
                    noteViewData,
                )
            }
        }
    }.catch {
        logger.error("observe notifications error", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PageableState.Loading.Init())

    val notifications = notificationPageableState.map {
        it.toList()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        listOf(NotificationListItem.Loading)
    )


    val isLoading = notificationPageableState.map {
        it is PageableState.Loading
    }.asLiveData()
    private val _error = MutableSharedFlow<Throwable>(
        onBufferOverflow = BufferOverflow.DROP_LATEST,
        extraBufferCapacity = 100
    )

    private val logger = loggerFactory.create("NotificationViewModel")

    init {
        accountStore.observeCurrentAccount.filterNotNull().flowOn(Dispatchers.IO)
            .onEach {
                loadInit()
            }.launchIn(viewModelScope)

        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
            notificationStreaming.connect {
                ac
            }.map {
                ac to it
            }
        }.buffer(100).catch { e ->
            logger.warning("ストーリミング受信中にエラー発生", e = e)
        }.onEach {
            notificationPagingStore.onReceiveNewNotification(it.second)
        }.launchIn(viewModelScope + Dispatchers.IO)

        //loadInit()
    }

    fun loadInit() {
        viewModelScope.launch {
            notificationPagingStore.clear()
            notificationPagingStore.loadPrevious().onFailure {
                logger.error("通知の読み込みに失敗", it)
                _error.tryEmit(it)
            }
        }
    }

    fun loadOld() {
        viewModelScope.launch {
            notificationPagingStore.loadPrevious().onFailure {
                logger.error("通知の読み込みに失敗", it)
                _error.tryEmit(it)
            }
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

}

sealed interface NotificationListItem {
    data class Notification(
        val notificationViewData: NotificationViewData
    ) : NotificationListItem

    object Loading : NotificationListItem

    data class Error(val throwable: Throwable) : NotificationListItem {

        fun getErrorMessage(): StringSource {
            return when(throwable) {
                is APIError -> {
                    APIErrorStringConverter()(throwable)
                }
                else -> StringSource("Error: $throwable")
            }
        }

        fun isUnauthorizedError(): Boolean {
            return throwable is APIError.AuthenticationException
                    || throwable is APIError.ForbiddenException
                    || throwable is UnauthorizedException
        }
    }

    object Empty : NotificationListItem
}

fun PageableState<List<NotificationViewData>>.toList(): List<NotificationListItem> {
    return when (val content = this.content) {
        is StateContent.Exist -> {
            content.rawContent.map { viewData ->
                NotificationListItem.Notification(viewData)
            }
        }
        is StateContent.NotExist -> {
            when (this) {
                is PageableState.Error -> {
                    listOf(NotificationListItem.Error(this.throwable))
                }
                is PageableState.Fixed -> listOf(NotificationListItem.Empty)
                is PageableState.Loading -> listOf(NotificationListItem.Loading)
            }
        }
    }
}