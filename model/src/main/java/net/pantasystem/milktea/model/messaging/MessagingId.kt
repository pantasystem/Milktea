package net.pantasystem.milktea.model.messaging

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
        val userId: net.pantasystem.milktea.model.user.User.Id
    ) : MessagingId() {
        constructor(message: Message.Direct, account: net.pantasystem.milktea.model.account.Account) : this(message.partnerUserId(account))
    }


}