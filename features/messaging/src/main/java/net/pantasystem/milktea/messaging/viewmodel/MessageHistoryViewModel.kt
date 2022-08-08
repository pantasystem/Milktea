package net.pantasystem.milktea.messaging.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.messaging.*
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessageHistoryViewModel @Inject constructor(
    accountStore: AccountStore,
    loggerFactory: Logger.Factory,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val groupRepository: GroupRepository,
    private val messageObserver: MessageObserver,
    private val messagingRepository: MessagingRepository,
) : ViewModel() {


    private val logger = loggerFactory.create("MessageHistoryViewModel")

    private val _actionFetchMessageHistories = MutableSharedFlow<Long>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 100
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    private val fetchUserMsgHistories = _actionFetchMessageHistories.map {
        logger.debug("読み込み命令を検出")
        accountRepository.getCurrentAccount().getOrThrow()
    }.filterNotNull().flatMapLatest {
        fetchHistory(false, it)
    }.catch { e ->
        emit(ResultState.Error(StateContent.NotExist(), e))
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        ResultState.Loading(StateContent.NotExist())
    ).stateIn(viewModelScope, SharingStarted.Eagerly, ResultState.Loading(StateContent.NotExist()))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val fetchGroupMsgHistories = _actionFetchMessageHistories.map {
        logger.debug("読み込み命令を検出")
        accountRepository.getCurrentAccount().getOrThrow()
    }.filterNotNull().flatMapLatest {
        fetchHistory(true, it)
    }.catch { e ->
        emit(ResultState.Error(StateContent.NotExist(), e))
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        ResultState.Loading(StateContent.NotExist())
    ).stateIn(viewModelScope, SharingStarted.Eagerly, ResultState.Loading(StateContent.NotExist()))

    private val usersAndGroups =
        combine(fetchUserMsgHistories, fetchGroupMsgHistories) { users, groups ->
            logger.debug("users($users), groups($groups)")
            val content =
                if (users.content is StateContent.Exist || groups.content is StateContent.Exist) {
                    val userList = (users.content as? StateContent.Exist)?.rawContent ?: emptyList()
                    val groupList =
                        (groups.content as? StateContent.Exist)?.rawContent ?: emptyList()
                    StateContent.Exist(groupList + userList)
                } else {
                    StateContent.NotExist()
                }
            if (users is ResultState.Loading && groups is ResultState.Loading) {
                ResultState.Loading(content)
            } else if (users is ResultState.Error && groups is ResultState.Error) {
                ResultState.Error(content, users.throwable)
            } else {
                ResultState.Fixed(content)
            }
        }.map {
            it.convert { list ->
                list.distinctBy { msg ->
                    msg.messagingId
                }
            }
        }.flowOn(Dispatchers.IO)

    private val histories = usersAndGroups.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ResultState.Loading(StateContent.NotExist())
    )

    private val isUserNameDefault = MutableStateFlow(false)

    val uiState = combine(histories, isUserNameDefault) { histories, configState ->
        MessageHistoryScreenUiState(configState, histories)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        MessageHistoryScreenUiState()
    )

    val isRefreshing = combine(fetchUserMsgHistories, fetchGroupMsgHistories) { users, groups ->
        users is ResultState.Loading || groups is ResultState.Loading
    }.asLiveData()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
                messageObserver.observeAccountMessages(it)
            }.collect {
                _actionFetchMessageHistories.tryEmit(Clock.System.now().toEpochMilliseconds())
            }
        }
        viewModelScope.launch {
            accountStore.observeCurrentAccount.distinctUntilChanged().collect {
                loadGroupAndUser()
            }
        }
    }

    fun loadGroupAndUser() {
        val result = _actionFetchMessageHistories.tryEmit(Clock.System.now().toEpochMilliseconds())
        logger.debug("メッセージ一覧読み込み pub :$result")
    }


    private suspend fun fetchHistory(
        isGroup: Boolean,
        account: Account
    ): Flow<ResultState<List<MessageHistoryRelation>>> {
        logger.debug("fetchHistory")
        return suspend {
            messagingRepository.findMessageSummaries(account.accountId, isGroup).map { list ->
                list.map {
                    it.toHistory(groupRepository, userRepository)
                }
            }.onFailure {
                logger.error("fetchMessagingHistory error", e = it)
            }.getOrThrow()
        }.asLoadingStateFlow()

    }


}

data class MessageHistoryScreenUiState(
    val isUserNameDefault: Boolean = true,
    val histories: ResultState<List<MessageHistoryRelation>> = ResultState.Loading(StateContent.NotExist())
)