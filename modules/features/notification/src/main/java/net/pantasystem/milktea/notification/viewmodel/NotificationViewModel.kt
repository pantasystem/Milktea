package net.pantasystem.milktea.notification.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.common_android_ui.APIErrorStringConverter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.filter.WordFilterService
import net.pantasystem.milktea.model.group.AcceptGroupInvitationUseCase
import net.pantasystem.milktea.model.group.RejectGroupInvitationUseCase
import net.pantasystem.milktea.model.notification.*
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.follow.requests.AcceptFollowRequestUseCase
import net.pantasystem.milktea.model.user.follow.requests.RejectFollowRequestUseCase
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val notificationStreaming: NotificationStreaming,
    private val notificationRepository: NotificationRepository,
    private val noteWordFilterService: WordFilterService,
    private val configRepository: LocalConfigRepository,
    private val acceptFollowRequestUseCase: AcceptFollowRequestUseCase,
    private val rejectFollowRequestUseCase: RejectFollowRequestUseCase,
    private val acceptGroupInvitationUseCase: AcceptGroupInvitationUseCase,
    private val rejectGroupInvitationUseCase: RejectGroupInvitationUseCase,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    loggerFactory: Logger.Factory,
    accountStore: AccountStore,
    notificationPagingStoreFactory: NotificationPagingStore.Factory,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_SPECIFIED_ACCOUNT_ID = "NotificationViewModel.EXTRA_SPECIFIED_ACCOUNT_ID"
    }

    private val planeNoteViewDataCache: PlaneNoteViewDataCache =
        planeNoteViewDataCacheFactory.create({
            getCurrentAccount()
        }, viewModelScope)

    private val notificationPagingStore = notificationPagingStoreFactory.create {
        getCurrentAccount()
    }

    private val notificationPageableState = notificationPagingStore.notifications.map { state ->
        state.suspendConvert { list ->
            list.filterNot {
                noteWordFilterService.isShouldFilterNote(Pageable.Notification(), it.note)
            }.map { n ->
                val noteViewData = n.note?.let {
                    planeNoteViewDataCache.get(it)
                }
                NotificationViewData(
                    n,
                    noteViewData,
                    configRepository,
                    viewModelScope,
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
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false
    )

    private val _error = MutableSharedFlow<Throwable>(
        onBufferOverflow = BufferOverflow.DROP_LATEST,
        extraBufferCapacity = 100
    )

    val errors = _error.asSharedFlow()

    private val logger = loggerFactory.create("NotificationViewModel")

    private val currentAccount = savedStateHandle.getStateFlow<Long?>(EXTRA_SPECIFIED_ACCOUNT_ID, null).flatMapLatest { accountId ->
        accountStore.getOrCurrent(accountId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var listenStreamingJob: Job? = null

    init {
        currentAccount.filterNotNull().flowOn(Dispatchers.IO)
            .onEach {
                loadInit()
            }.launchIn(viewModelScope)

        //loadInit()
    }

    fun onResume() {
        listenStreamingJob?.cancel()
        listenStreamingJob = currentAccount.filterNotNull().flatMapLatest { ac ->
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

    }

    fun onPause() {
        listenStreamingJob?.cancel()
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
                acceptFollowRequestUseCase(notification.userId).onSuccess {
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
                rejectFollowRequestUseCase(notification.userId).onSuccess {
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
                acceptGroupInvitationUseCase(notification.invitationId)
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
                rejectGroupInvitationUseCase(notification.invitationId)
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

    fun onMarkAsReadAllNotifications() {
        viewModelScope.launch {
            runCancellableCatching {
                getCurrentAccount()
            }.mapCancellableCatching {
                notificationRepository.markAsRead(it.accountId)
            }.onSuccess {
                loadInit()
            }.onFailure {
                logger.error("failed mark as read", it)
                _error.tryEmit(it)
            }
        }
    }

    private suspend fun getCurrentAccount(): Account {
        return savedStateHandle.get<Long>(EXTRA_SPECIFIED_ACCOUNT_ID)?.let { accountId ->
            return accountRepository.get(accountId).getOrThrow()
        } ?: accountRepository.getCurrentAccount().getOrThrow()
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