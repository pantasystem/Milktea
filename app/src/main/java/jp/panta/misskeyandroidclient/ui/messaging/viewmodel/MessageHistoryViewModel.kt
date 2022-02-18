package jp.panta.misskeyandroidclient.ui.messaging.viewmodel

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.groups.toGroup
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.messaging.toHistory
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.util.asLoadingStateFlow
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

@ExperimentalCoroutinesApi
@FlowPreview
class MessageHistoryViewModel(
    private val miCore: MiCore,
    private val encryption: Encryption = miCore.getEncryption(),
) : ViewModel() {


    private val logger = miCore.loggerFactory.create("MessageHistoryViewModel")

    private val _actionFetchMessageHistories = MutableSharedFlow<Long>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 100
    )

    private val updateEvent =
        miCore.getAccountStore().observeCurrentAccount.filterNotNull().flatMapLatest { account ->
            miCore.messageObserver.observeAccountMessages(account).map {
                account to miCore.getGetters().messageRelationGetter.get(it)
            }
        }.map { (a, msg) ->
            a to msg.toHistory(miCore.getGroupRepository(), miCore.getUserRepository())
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val fetchUserMsgHistories = _actionFetchMessageHistories.map {
        logger.debug("読み込み命令を検出")
        miCore.getAccountRepository().getCurrentAccount()
    }.filterNotNull().flatMapLatest {
        suspend {
            fetchHistory(false, it)
        }.asLoadingStateFlow()
    }.flowOn(Dispatchers.IO)

    private val fetchGroupMsgHistories = _actionFetchMessageHistories.map {
        logger.debug("読み込み命令を検出")
        miCore.getAccountRepository().getCurrentAccount()
    }.filterNotNull().flatMapLatest {
        suspend {
            fetchHistory(true, it)
        }.asLoadingStateFlow()
    }.flowOn(Dispatchers.IO)

    private val usersAndGroups =
        combine(fetchUserMsgHistories, fetchGroupMsgHistories) { users, groups ->
            val u = (users.content as? StateContent.Exist)?.rawContent
            val g = (groups.content as? StateContent.Exist)?.rawContent
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
                miCore.getUnreadMessages(),
                viewModelScope,
            )
        } else {
            list.map {
                if (it.messagingId == anyMsg.messagingId) {
                    HistoryViewData(
                        a,
                        ev,
                        miCore.getUnreadMessages(),
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
        users is State.Loading || groups is State.Loading
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
                    miCore.getGroupDataSource().add(groupDTO.toGroup(account.accountId))
                }
                it.recipient?.let { userDTO ->
                    miCore.getUserDataSource().add(userDTO.toUser(account))
                }
                miCore.getGetters().messageRelationGetter.get(account, it)
            }
        }.onFailure {
            logger.error("fetchMessagingHistory error", e = it)
        }.getOrNull()?.map {
            it.toHistory(miCore.getGroupRepository(), miCore.getUserRepository())
        }?.map {
            HistoryViewData(account, it, miCore.getUnreadMessages(), viewModelScope)
        } ?: emptyList()
    }

    fun openMessage(messageHistory: HistoryViewData) {
        messageHistorySelected.event = messageHistory
    }

    private fun getMisskeyAPI(account: Account): MisskeyAPI {
        return miCore.getMisskeyAPIProvider().get(account)
    }

}