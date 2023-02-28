package net.pantasystem.milktea.messaging.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.messaging.MessagePagingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessageObserver
import net.pantasystem.milktea.model.messaging.MessageRelationGetter
import net.pantasystem.milktea.model.messaging.MessagingId
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messagePagingStore: MessagePagingStore,
    private val messageRelationGetter: MessageRelationGetter,
    private val messageObserver: MessageObserver,
    private val accountStore: AccountStore,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
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
                runCancellableCatching {
                    messageRelationGetter.get(id)
                }.getOrNull()
            }.asReversed()
        }
    }.flowOn(Dispatchers.IO).catch {
        logger.debug(e = it) { "message error" }
    }.stateIn(viewModelScope, SharingStarted.Lazily, PageableState.Loading.Init())


    val title: LiveData<String> = MutableLiveData("")

    val account = accountStore.observeCurrentAccount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

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


    fun loadOld() {
        viewModelScope.launch {
            messagePagingStore.loadPrevious()
        }
    }

    fun setMessagingId(messagingId: MessagingId) {
        viewModelScope.launch {
            messagePagingStore.setMessagingId(messagingId)
            messagePagingStore.clear()
            messagePagingStore.loadPrevious()
            (title as MutableLiveData).postValue(loadMessageTitle(messagingId).getOrNull() ?: "")
        }
    }

    private suspend fun loadMessageTitle(messagingId: MessagingId): Result<String> {
        return runCancellableCatching {
            when (messagingId) {
                is MessagingId.Direct -> {
                    userRepository.find(messagingId.userId).displayUserName
                }
                is MessagingId.Group -> {
                    groupRepository.syncOne(messagingId.groupId).name
                }
            }
        }
    }

}
