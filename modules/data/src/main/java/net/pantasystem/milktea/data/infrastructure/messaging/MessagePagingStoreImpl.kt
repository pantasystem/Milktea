package net.pantasystem.milktea.data.infrastructure.messaging

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.api.misskey.messaging.RequestMessage
import net.pantasystem.milktea.app_store.messaging.MessagePagingStore
import net.pantasystem.milktea.app_store.messaging.MessagingPagingState
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject

class MessagePagingStoreImpl @Inject constructor(
    val getAccount: GetAccount,
    val misskeyAPIProvider: MisskeyAPIProvider,
    messageAdder: MessageAdder,
) : MessagePagingStore {

    private val messagePagingModel: MessagePagingModel = MessagePagingModel(
        getAccount = getAccount,
        misskeyAPIProvider = misskeyAPIProvider,
        messageAdder = messageAdder,
    )


    private val receivedMessageQueue = MutableSharedFlow<Message.Id>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1000
    )

    override suspend fun setMessagingId(messagingId: MessagingId) {
        this.messagePagingModel.messagingId = messagingId
    }


    override val state: Flow<MessagingPagingState>
        get() = messagePagingModel.state.map {
            MessagingPagingState(it)
        }

    private var latestReceivedMessageId: Message.Id? = null

    private val previousPagingController = MessagingPreviousPaging(
        messagePagingModel,
        messagePagingModel,
        messagePagingModel,
        messagePagingModel,
    )


    override suspend fun clear() {
        messagePagingModel.mutex.withLock {
            messagePagingModel.setState(PageableState.Loading.Init())
        }
    }


    override suspend fun loadPrevious() {
        latestReceivedMessageId = null
        previousPagingController.loadPrevious()
    }

    override fun onReceiveMessage(msg: Message.Id) {
        receivedMessageQueue.tryEmit(msg)
    }

    override fun latestReceivedMessageId(): Message.Id? {
        return latestReceivedMessageId
    }

    override suspend fun collectReceivedMessageQueue(): Nothing {
        receivedMessageQueue.collect { msgId ->
            messagePagingModel.mutex.withLock {
                val state = messagePagingModel.getState()
                val existsItem = when (val content = state.content) {
                    is StateContent.Exist -> {
                        content.rawContent.contains(msgId)
                    }
                    is StateContent.NotExist -> false
                }
                if(!existsItem) {
                    latestReceivedMessageId = msgId
                }
                messagePagingModel.setState(
                    state.convert {
                        if (existsItem) {
                            it
                        } else {
                            it + msgId
                        }
                    }
                )
            }
        }
    }
}

class MessagingPreviousPaging<DTO, E>(
    private val entityConverter: EntityConverter<DTO, E>,
    private val locker: StateLocker,
    private val state: PaginationState<E>,
    private val previousLoader: PreviousLoader<DTO>
) {


    suspend fun loadPrevious() {
        locker.mutex.withLock {
            runCancellableCatching {
                val body = previousLoader.loadPrevious().getOrThrow().asReversed()
                entityConverter.convertAll(body)
            }.onFailure {
                val errorState = PageableState.Error(
                    state.getState().content,
                    it
                )
                state.setState(errorState)
            }.onSuccess {
                when(val content = this.state.getState().content) {
                    is StateContent.Exist -> {
                        val newList = content.rawContent.toMutableList()
                        newList.addAll(0, it)
                        state.setState(
                            PageableState.Fixed(
                                StateContent.Exist(
                                    newList
                                )
                            )
                        )
                    }
                    is StateContent.NotExist -> {
                        state.setState(
                            PageableState.Fixed(
                                StateContent.Exist(it)
                            )
                        )
                    }
                }
            }
        }
    }

}


class MessagePagingModel(
    val getAccount: GetAccount,
    val misskeyAPIProvider: MisskeyAPIProvider,
    private val messageAdder: MessageAdder,
    var messagingId: MessagingId? = null,
) : StateLocker, PreviousLoader<MessageDTO>, FutureLoader<MessageDTO>,
    IdGetter<Message.Id>, PaginationState<Message.Id>, EntityConverter<MessageDTO, Message.Id> {

    override val mutex: Mutex = Mutex()

    private val _state = MutableStateFlow<PageableState<List<Message.Id>>>(
        PageableState.Loading.Init()
    )

    override val state: Flow<PageableState<List<Message.Id>>> = _state

    override suspend fun getSinceId(): Message.Id? {
        return null
    }

    override fun getState(): PageableState<List<Message.Id>> {
        return _state.value
    }

    override suspend fun getUntilId(): Message.Id? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.firstOrNull()
    }

    override suspend fun loadFuture(): Result<List<MessageDTO>> = runCancellableCatching {
        require(messagingId != null) {
            "messagingIdがNullの段階で読み込みはできません"
        }
        val account = getAccount.get(messagingId!!.accountId)
        misskeyAPIProvider.get(account).getMessages(
            RequestMessage(
                i = account.token,
                sinceId = getSinceId()?.messageId,
                groupId = (messagingId as? MessagingId.Group)?.groupId?.groupId,
                userId = (messagingId as? MessagingId.Direct)?.userId?.id
            )
        ).throwIfHasError().body()!!
    }

    override suspend fun loadPrevious(): Result<List<MessageDTO>> = runCancellableCatching {
        require(messagingId != null) {
            "messagingIdがNullの段階で読み込みはできません"
        }
        val account = getAccount.get(messagingId!!.accountId)
        misskeyAPIProvider.get(account).getMessages(
            RequestMessage(
                i = account.token,
                untilId = getUntilId()?.messageId,
                groupId = (messagingId as? MessagingId.Group)?.groupId?.groupId,
                userId = (messagingId as? MessagingId.Direct)?.userId?.id
            )
        ).throwIfHasError().body()!!
    }

    override fun setState(state: PageableState<List<Message.Id>>) {
        _state.value = state
    }

    override suspend fun convertAll(list: List<MessageDTO>): List<Message.Id> {
        val account = getAccount.get(messagingId!!.accountId)
        return list.map {
            messageAdder.add(account, it)
        }.map {
            it.message.id
        }
    }
}