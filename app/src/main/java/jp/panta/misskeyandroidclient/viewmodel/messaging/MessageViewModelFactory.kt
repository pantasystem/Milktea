package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.model.group.Group
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessagingId
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class MessageViewModelFactory(
    private val messagingId: MessagingId,
    private val miCore: MiCore
): ViewModelProvider.Factory{

    constructor(userId: User.Id, miCore: MiCore) : this(MessagingId.Direct(userId), miCore)
    constructor(groupId: Group.Id, miCore: MiCore) : this(MessagingId.Group(groupId), miCore)

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == MessageViewModel::class.java){
            return MessageViewModel(miCore, messagingId) as T
        }
        throw IllegalArgumentException("use MessageViewModel::class.java")
    }
}