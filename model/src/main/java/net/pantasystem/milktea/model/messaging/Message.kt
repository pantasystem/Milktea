package net.pantasystem.milktea.model.messaging

import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.data.model.group.GroupRepository
import net.pantasystem.milktea.data.model.group.Group as GroupEntity
import kotlinx.datetime.Instant


sealed class Message{
    data class Id(
        val accountId: Long,
        val messageId: String
    ) : EntityId

    abstract val id: Id
    abstract val createdAt: Instant
    abstract val text: String?
    abstract val userId: net.pantasystem.milktea.model.user.User.Id
    abstract val fileId: String?
    abstract val file: net.pantasystem.milktea.model.drive.FileProperty?
    abstract val isRead: Boolean
    abstract val emojis: List<net.pantasystem.milktea.model.emoji.Emoji>

    /**
     * isReadをtrueにして新しいオブジェクトを返す
     */
    abstract fun read(): Message

    fun messagingId(account: net.pantasystem.milktea.model.account.Account): MessagingId {
        return when(this) {
            is Direct -> MessagingId.Direct(this, account)
            is Group -> MessagingId.Group(groupId)
        }
    }



    data class Group(
        override val id: Id,
        override val createdAt: Instant,
        override val text: String?,
        override val userId: net.pantasystem.milktea.model.user.User.Id,
        override val fileId: String?,
        override val file: net.pantasystem.milktea.model.drive.FileProperty?,
        override val isRead: Boolean,
        override val emojis: List<net.pantasystem.milktea.model.emoji.Emoji>,
        val groupId: GroupEntity.Id,
    ) : Message() {
        override fun read(): Message {
            return this.copy(isRead = true)
        }
    }

    /**
     * @param recipientId 受信者のUser.Id
     */
    data class Direct(
        override val id: Id,
        override val createdAt: Instant,
        override val text: String?,
        override val userId: net.pantasystem.milktea.model.user.User.Id,
        override val fileId: String?,
        override val file: net.pantasystem.milktea.model.drive.FileProperty?,
        override val isRead: Boolean,
        override val emojis: List<net.pantasystem.milktea.model.emoji.Emoji>,
        val recipientId: net.pantasystem.milktea.model.user.User.Id
    ) : Message() {
        override fun read(): Message {
            return this.copy(isRead = true)
        }

        fun partnerUserId(account: net.pantasystem.milktea.model.account.Account): net.pantasystem.milktea.model.user.User.Id {
            return if (recipientId == net.pantasystem.milktea.model.user.User.Id(account.accountId, account.remoteId)) {
                userId
            } else {
                recipientId
            }
        }
    }




}


sealed class CreateMessage {
    abstract val accountId: Long
    abstract val text: String?
    abstract val fileId: String?

    data class Direct(
        override val accountId: Long,
        val userId: net.pantasystem.milktea.model.user.User.Id,
        override val text: String?,
        override val fileId: String?,
    ) : CreateMessage()

    data class Group(
        override val accountId: Long,
        val groupId: GroupEntity.Id,
        override val text: String?,
        override val fileId: String?
    ) : CreateMessage()

    object Factory {

        fun create(messagingId: MessagingId, text: String?, fileId: String?): CreateMessage {
            return when(messagingId) {
                is MessagingId.Direct -> {
                    Direct(
                        messagingId.accountId,
                        messagingId.userId,
                        text,
                        fileId
                    )
                }
                is MessagingId.Group -> {
                    Group(
                        messagingId.accountId,
                        messagingId.groupId,
                        text,
                        fileId
                    )
                }
            }
        }
    }
}

sealed class MessageRelation {

    abstract val message: Message
    abstract val user: net.pantasystem.milktea.model.user.User

    data class Group(
        override val message: Message.Group,
        override val user: net.pantasystem.milktea.model.user.User
    ) : MessageRelation()

    data class Direct(
        override val message: Message.Direct,
        override val user: net.pantasystem.milktea.model.user.User,
    ) : MessageRelation()

    fun isMime(account: net.pantasystem.milktea.model.account.Account): Boolean {
        return message.userId == net.pantasystem.milktea.model.user.User.Id(account.accountId, account.remoteId)
    }
}

sealed class MessageHistoryRelation : MessageRelation(){
    data class Group(
        override val message: Message,
        override val user: net.pantasystem.milktea.model.user.User,
        val group: GroupEntity
    ) : MessageHistoryRelation()

    data class Direct(
        override val message: Message,
        override val user: net.pantasystem.milktea.model.user.User,
        val recipient: net.pantasystem.milktea.model.user.User
    ) : MessageHistoryRelation()
}

suspend fun MessageRelation.toHistory(groupRepository: GroupRepository, userRepository: net.pantasystem.milktea.model.user.UserRepository): MessageHistoryRelation {
    return when(val msg = message) {
        is Message.Direct -> {
            val recipient = userRepository.find(msg.recipientId, false)
            MessageHistoryRelation.Direct(this.message, this.user, recipient)
        }
        is Message.Group -> {
            val group = groupRepository.find(msg.groupId)
            MessageHistoryRelation.Group(this.message, this.user, group)
        }
    }

}