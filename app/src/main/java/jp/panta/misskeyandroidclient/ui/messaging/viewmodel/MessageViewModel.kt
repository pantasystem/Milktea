package jp.panta.misskeyandroidclient.ui.messaging.viewmodel

import androidx.lifecycle.*
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.data.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.data.api.misskey.messaging.RequestMessage
import net.pantasystem.milktea.model.messaging.MessagingId
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

@ExperimentalCoroutinesApi
@FlowPreview
class MessageViewModel(
    private val miCore: MiCore,
    private val messagingId: net.pantasystem.milktea.model.messaging.MessagingId,
) : ViewModel() {

    class State(
        val messages: List<MessageViewData>,
        val type: Type
    ) {
        enum class Type {
            LOAD_INIT, LOAD_OLD, RECEIVED
        }
    }

    val messagesLiveData = MutableLiveData<State>()

    private var isLoading = false


    private val mTitle = MutableLiveData<String>().apply {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                when (messagingId) {
                    is net.pantasystem.milktea.model.messaging.MessagingId.Direct -> {
                        miCore.getUserRepository().find(messagingId.userId).getDisplayUserName()
                    }
                    is net.pantasystem.milktea.model.messaging.MessagingId.Group -> {
                        miCore.getGroupRepository().find(messagingId.groupId).name
                    }
                }
            }.onSuccess {
                postValue(it)
            }.onFailure {
                logger.debug("タイトル取得の失敗", e = it)
            }
        }
    }
    val title: LiveData<String> = mTitle

    private val logger = miCore.loggerFactory.create("MessageViewModel")

    init {
        miCore.messageObserver.observeByMessagingId(messagingId).map {
            miCore.getGetters().messageRelationGetter.get(it)
        }.catch { e ->
            logger.warning("ストリーミング受信中にエラー発生", e = e)
        }.onEach { msg ->
            val messages = messagesLiveData.value?.messages.toArrayList()
            val a = messagingId.getAccount()
            val viewData = if (msg.isMime(a)) {
                SelfMessageViewData(msg, a)
            } else {
                OtherUserMessageViewData(msg, a)
            }
            logger.debug("onMessage: $msg")
            messages.add(viewData)

            messagesLiveData.postValue(State(messages, State.Type.RECEIVED))
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun loadInit() {
        if (isLoading) {
            logger.debug("load cancel")
            return
        }
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val account = messagingId.getAccount()
            val viewDataList = runCatching {
                miCore.getMisskeyAPIProvider().get(account).getMessages(
                    RequestMessage(
                        i = account.getI(miCore.getEncryption()),
                        groupId = (messagingId as? net.pantasystem.milktea.model.messaging.MessagingId.Group)?.groupId?.groupId,
                        userId = (messagingId as? net.pantasystem.milktea.model.messaging.MessagingId.Direct)?.userId?.id
                    ),
                ).throwIfHasError().body()?.asReversed()
            }.onFailure {
                logger.debug("メッセージの読み込みに失敗しました。", e = it)
            }.getOrNull()?.toMessageViewData(account) ?: emptyList()
            messagesLiveData.postValue(State(viewDataList, State.Type.LOAD_INIT))
            isLoading = false
        }

    }

    fun loadOld() {
        if (isLoading) {
            return
        }
        isLoading = true
        val exMessages = messagesLiveData.value?.messages
        val untilId = exMessages?.firstOrNull()?.id
        if (exMessages.isNullOrEmpty() || untilId == null) {
            isLoading = false
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val account = messagingId.getAccount()
            val viewData = runCatching {
                miCore.getMisskeyAPIProvider().get(messagingId.getAccount()).getMessages(
                    RequestMessage(
                        i = account.getI(miCore.getEncryption()),
                        untilId = untilId.messageId,
                        groupId = (messagingId as? net.pantasystem.milktea.model.messaging.MessagingId.Group)?.groupId?.groupId,
                        userId = (messagingId as? net.pantasystem.milktea.model.messaging.MessagingId.Direct)?.userId?.id
                    )
                ).body()?.asReversed()
            }.getOrNull()?.toMessageViewData(account) ?: emptyList()

            val messages = ArrayList<MessageViewData>(exMessages).apply {
                addAll(0, viewData)
            }
            messagesLiveData.postValue(State(messages, State.Type.LOAD_OLD))
            isLoading = false
        }
    }

    private suspend fun List<MessageDTO>.toMessageViewData(account: net.pantasystem.milktea.model.account.Account): List<MessageViewData> {
        return this.map {
            miCore.getGetters().messageRelationGetter.get(account, it)
        }.map { msg ->
            if (msg.isMime(account)) {
                SelfMessageViewData(msg, account)
            } else {
                OtherUserMessageViewData(msg, account)
            }
        }
    }

    private fun List<MessageViewData>?.toArrayList(): ArrayList<MessageViewData> {
        return if (this == null) {
            ArrayList()
        } else {
            ArrayList(this)
        }
    }

    private suspend fun net.pantasystem.milktea.model.messaging.MessagingId.getAccount(): net.pantasystem.milktea.model.account.Account {
        val accountId = when (this) {
            is net.pantasystem.milktea.model.messaging.MessagingId.Direct -> this.userId.accountId
            is net.pantasystem.milktea.model.messaging.MessagingId.Group -> this.groupId.accountId
        }
        return miCore.getAccountRepository().get(accountId)
    }
}
