package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.users.User
import java.io.Serializable

sealed class MessagingId : Serializable{

    data class Group(
        val groupId: String
    ) : MessagingId()

    data class Direct(
        val userId: User.Id
    ) : MessagingId() {
        constructor(message: Message.Direct, account: Account) : this(message.partnerUserId(account))
    }


}