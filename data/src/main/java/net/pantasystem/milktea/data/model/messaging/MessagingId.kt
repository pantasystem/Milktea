package net.pantasystem.milktea.data.model.messaging

import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.users.User
import java.io.Serializable
import net.pantasystem.milktea.data.model.group.Group as GroupEntity

sealed class MessagingId : Serializable{

    val accountId: Long
        get() {
            return when(this) {
                is Group -> {
                    groupId.accountId
                }
                is Direct -> {
                    userId.accountId
                }
            }
        }

    data class Group(
        val groupId: GroupEntity.Id
    ) : MessagingId()

    data class Direct(
        val userId: User.Id
    ) : MessagingId() {
        constructor(message: Message.Direct, account: Account) : this(message.partnerUserId(account))
    }


}