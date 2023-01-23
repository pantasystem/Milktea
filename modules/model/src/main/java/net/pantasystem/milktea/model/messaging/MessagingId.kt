package net.pantasystem.milktea.model.messaging

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import java.io.Serializable
import net.pantasystem.milktea.model.group.Group as GroupEntity

sealed class MessagingId : Serializable {

    val accountId: Long
        get() {
            return when (this) {
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
        constructor(
            message: Message.Direct,
            account: Account
        ) : this(message.partnerUserId(account))
    }


}