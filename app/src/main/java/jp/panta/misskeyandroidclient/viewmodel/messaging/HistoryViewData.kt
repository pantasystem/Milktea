package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageRelation
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessageStore
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessages
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

}