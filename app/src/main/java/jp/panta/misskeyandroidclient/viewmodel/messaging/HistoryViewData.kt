package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageRelation
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessages
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class HistoryViewData (account: Account, message: MessageRelation, unReadMessages: UnReadMessages, coroutineScope: CoroutineScope, coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO){
    val messagingId = message.message.messagingId(account)
    val message = MutableLiveData<MessageRelation>(message)
    //val id = message.message.id
    val isGroup = message is MessageRelation.Group
    val group = (message as? MessageRelation.Group)?.group
    val partner = (message as? MessageRelation.Direct)?.let {
        if(message.recipient.id.id == account.remoteId){
            message.user
        }else{
            message.recipient
        }
    }

    val historyIcon = if(isGroup) {
        message.user.avatarUrl
    }else{
        partner?.avatarUrl
    }


    val title = if(isGroup){
        "${group?.name}"
    }else{
        val host = partner?.host
        "@${partner?.userName}" + if(host != null) "@$host" else ""
    }
    private val mUnreadMessages = MutableLiveData<List<Message>>()
    val unreadMessages: LiveData<List<Message>> = mUnreadMessages
    val unreadMessageCount = Transformations.map(mUnreadMessages){
        it?.size?: 0
    }

    private val scope = coroutineScope + coroutineDispatcher
    init {
        scope.launch {
            unReadMessages.findByMessagingId(messagingId).collect {
                mUnreadMessages.postValue(it)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryViewData

        if (messagingId != other.messagingId) return false
        if (message != other.message) return false
        if (isGroup != other.isGroup) return false
        if (group != other.group) return false
        if (partner != other.partner) return false
        if (historyIcon != other.historyIcon) return false
        if (title != other.title) return false
        if (mUnreadMessages != other.mUnreadMessages) return false
        if (unreadMessages != other.unreadMessages) return false
        if (unreadMessageCount != other.unreadMessageCount) return false
        if (scope != other.scope) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messagingId.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + isGroup.hashCode()
        result = 31 * result + (group?.hashCode() ?: 0)
        result = 31 * result + (partner?.hashCode() ?: 0)
        result = 31 * result + (historyIcon?.hashCode() ?: 0)
        result = 31 * result + title.hashCode()
        result = 31 * result + mUnreadMessages.hashCode()
        result = 31 * result + unreadMessages.hashCode()
        result = 31 * result + unreadMessageCount.hashCode()
        result = 31 * result + scope.hashCode()
        return result
    }

}