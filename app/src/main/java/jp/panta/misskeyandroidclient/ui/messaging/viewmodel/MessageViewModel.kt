package jp.panta.misskeyandroidclient.ui.messaging.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.messaging.*
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messagePagingStore: MessagePagingStore,
    private val messageRelationGetter: MessageRelationGetter,
    private val messageObserver: MessageObserver,
    private val accountStore: AccountStore,
    private val groupRepository: GroupRepository,
    private  val userRepository: UserRepository,
    loggerFactory: Logger.Factory,
) : ViewModel() {


    private val logger by lazy {
        loggerFactory.create("MessageViewModel")
    }

    val latestReceivedMessageId: Message.Id?
        get() = messagePagingStore.latestReceivedMessageId()

    val messages = messagePagingStore.state.map { state ->
        state.pageState.suspendConvert { list ->
            list.mapNotNull { id ->
                runCatching {
                    messageRelationGetter.get(id)
                }.getOrNull()
            }.asReversed()
        }
    }.flowOn(Dispatchers.IO).catch {
        logger.debug("message error", e = it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PageableState.Loading.Init())


    val title: LiveData<String> = MutableLiveData("")

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        viewModelScope.launch(Dispatchers.IO) {
            accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
                messageObserver.observeAccountMessages(it)
            }.collect {
                messagePagingStore.onReceiveMessage(it.id)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            messagePagingStore.collectReceivedMessageQueue()
        }
    }

    fun loadInit() {
        viewModelScope.launch(Dispatchers.IO) {
            messagePagingStore.clear()
            messagePagingStore.loadPrevious()
        }
    }

    fun loadOld() {
        viewModelScope.launch(Dispatchers.IO) {
            messagePagingStore.loadPrevious()
        }
    }

    fun setMessagingId(messagingId: MessagingId) {
        viewModelScope.launch(Dispatchers.IO) {
            messagePagingStore.setMessagingId(messagingId)
            messagePagingStore.clear()
            messagePagingStore.loadPrevious()
            (title as MutableLiveData).postValue(loadMessageTitle(messagingId).getOrNull() ?: "")
        }
    }

    private suspend fun loadMessageTitle(messagingId: MessagingId): Result<String> {
        return runCatching {
            when(messagingId) {
                is MessagingId.Direct -> {
                    userRepository.find(messagingId.userId).displayUserName
                }
                is MessagingId.Group -> {
                    groupRepository.find(messagingId.groupId).name
                }
            }
        }
    }

}
