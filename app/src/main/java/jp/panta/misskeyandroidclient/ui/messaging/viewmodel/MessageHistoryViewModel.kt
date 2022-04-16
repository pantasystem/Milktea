package jp.panta.misskeyandroidclient.ui.messaging.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPI
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.api.misskey.groups.toGroup
import net.pantasystem.milktea.data.api.misskey.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.users.toUser
import net.pantasystem.milktea.data.gettters.Getters
import net.pantasystem.milktea.data.model.Encryption
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.account.AccountRepository
import net.pantasystem.milktea.data.model.account.AccountStore
import net.pantasystem.milktea.data.model.group.GroupDataSource
import net.pantasystem.milktea.data.model.group.GroupRepository
import net.pantasystem.milktea.data.model.messaging.MessageObserver
import net.pantasystem.milktea.data.model.messaging.RequestMessageHistory
import net.pantasystem.milktea.data.model.messaging.UnReadMessages
import net.pantasystem.milktea.data.model.messaging.toHistory
import net.pantasystem.milktea.data.model.users.UserDataSource
import net.pantasystem.milktea.data.model.users.UserRepository
import net.pantasystem.milktea.common.State
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class MessageHistoryViewModel @Inject constructor(
    accountStore: AccountStore,
    loggerFactory: net.pantasystem.milktea.common.Logger.Factory,
    private val encryption: Encryption,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val groupDataSource: GroupDataSource,
    private val userDataSource: UserDataSource,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val getters: Getters,
    private val groupRepository: GroupRepository,
    private val messageObserver: MessageObserver,
    private val unreadMessages: UnReadMessages,
) : ViewModel() {


    private val logger = loggerFactory.create("MessageHistoryViewModel")

    private val _actionFetchMessageHistories = MutableSharedFlow<Long>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 100
    )

    private val updateEvent =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
            messageObserver.observeAccountMessages(account).map {
                account to getters.messageRelationGetter.get(it)
            }
        }.map { (a, msg) ->
            a to msg.toHistory(groupRepository, userRepository)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val fetchUserMsgHistories = _actionFetchMessageHistories.map {
        logger.debug("読み込み命令を検出")
        accountRepository.getCurrentAccount()
    }.filterNotNull().flatMapLatest {
        suspend {
            fetchHistory(false, it)
        }.asLoadingStateFlow()
    }.flowOn(Dispatchers.IO)

    private val fetchGroupMsgHistories = _actionFetchMessageHistories.map {
        logger.debug("読み込み命令を検出")
        accountRepository.getCurrentAccount()
    }.filterNotNull().flatMapLatest {
        suspend {
            fetchHistory(true, it)
        }.asLoadingStateFlow()
    }.flowOn(Dispatchers.IO)

    private val usersAndGroups =
        combine(fetchUserMsgHistories, fetchGroupMsgHistories) { users, groups ->
            val u = (users.content as? net.pantasystem.milktea.common.StateContent.Exist)?.rawContent
            val g = (groups.content as? net.pantasystem.milktea.common.StateContent.Exist)?.rawContent
            (g ?: emptyList()) + (u ?: emptyList())
        }.flowOn(Dispatchers.IO)

    val histories = combine(usersAndGroups, updateEvent) { list, pair ->
        if (pair == null) {
            return@combine list
        }
        val (a, ev) = pair
        val anyMsg = list.firstOrNull {
            it.messagingId == ev.message.messagingId(a)
        }
        val newList = if (anyMsg == null) {
            list + HistoryViewData(
                a,
                ev,
                unreadMessages,
                viewModelScope,
            )
        } else {
            list.map {
                if (it.messagingId == anyMsg.messagingId) {
                    HistoryViewData(
                        a,
                        ev,
                        unreadMessages,
                        viewModelScope,
                    )
                } else {
                    it
                }
            }
        }
        logger.debug("newList:$newList")
        newList
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, emptyList())

    val isRefreshing = combine(fetchUserMsgHistories, fetchGroupMsgHistories) { users, groups ->
        users is net.pantasystem.milktea.common.State.Loading || groups is net.pantasystem.milktea.common.State.Loading
    }.asLiveData()

    val messageHistorySelected = EventBus<HistoryViewData>()

    fun loadGroupAndUser() {
        val result = _actionFetchMessageHistories.tryEmit(Clock.System.now().toEpochMilliseconds())
        logger.debug("メッセージ一覧読み込み pub :$result")
    }


    private suspend fun fetchHistory(isGroup: Boolean, account: Account): List<HistoryViewData> {
        logger.debug("fetchHistory")
        val request =
            RequestMessageHistory(i = account.getI(encryption), group = isGroup, limit = 100)

        return runCatching {
            val res = getMisskeyAPI(account).getMessageHistory(request)
            res.throwIfHasError()
            res.body()?.map {
                it.group?.let { groupDTO ->
                    groupDataSource.add(groupDTO.toGroup(account.accountId))
                }
                it.recipient?.let { userDTO ->
                    userDataSource.add(userDTO.toUser(account))
                }
                getters.messageRelationGetter.get(account, it)
            }
        }.onFailure {
            logger.error("fetchMessagingHistory error", e = it)
        }.getOrNull()?.map {
            it.toHistory(groupRepository, userRepository)
        }?.map {
            HistoryViewData(account, it, unreadMessages, viewModelScope)
        } ?: emptyList()
    }

    fun openMessage(messageHistory: HistoryViewData) {
        messageHistorySelected.event = messageHistory
    }

    private fun getMisskeyAPI(account: Account): MisskeyAPI {
        return misskeyAPIProvider.get(account)
    }

}