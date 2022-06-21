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
import net.pantasystem.milktea.model.messaging.MessageRelationGetter
import net.pantasystem.milktea.data.infrastructure.messaging.impl.MessageObserver
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessagePagingStore
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messagePagingStore: MessagePagingStore,
    private val messageRelationGetter: MessageRelationGetter,
    private val messageObserver: MessageObserver,
    private val accountStore: AccountStore,
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
            }
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
        }
    }

}
