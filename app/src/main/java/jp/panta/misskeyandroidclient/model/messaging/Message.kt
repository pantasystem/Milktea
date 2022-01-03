package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.EntityId
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.group.GroupRepository
import jp.panta.misskeyandroidclient.model.group.Group as GroupEntity
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime


sealed class Message{
    data class Id(
        val accountId: Long,
        val messageId: String
    ) : EntityId

    abstract val id: Id
    abstract val createdAt: Instant
    abstract val text: String?
    abstract val userId: User.Id
    abstract val fileId: String?
    abstract val file: FileProperty?
    abstract val isRead: Boolean
    abstract val emojis: List<Emoji>

    /**
     * isReadをtrueにして新しいオブジェクトを返す
     */
    abstract fun read(): Message

    fun messagingId(account: Account): MessagingId {
        return when(this) {
            is Direct -> MessagingId.Direct(this, account)
            is Group -> MessagingId.Group(groupId)
        }
    }



    data class Group(
        override val id: Id,
        override val createdAt: Instant,
        override val text: String?,
        override val userId: User.Id,
        override val fileId: String?,
        override val file: FileProperty?,
        override val isRead: Boolean,
        override val emojis: List<Emoji>,
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
        override val userId: User.Id,
        override val fileId: String?,
        override val file: FileProperty?,
        override val isRead: Boolean,
        override val emojis: List<Emoji>,
        val recipientId: User.Id
    ) : Message() {
        override fun read(): Message {
            return this.copy(isRead = true)
        }

        fun partnerUserId(account: Account): User.Id {
            return if (recipientId == User.Id(account.accountId, account.remoteId)) {
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
        val userId: User.Id,
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

        fun create(messagingId: MessagingId, text: String?, fileId: String?): CreateMessage{
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
    abstract val user: User

    data class Group(
        override val message: Message.Group,
        override val user: User
    ) : MessageRelation()

    data class Direct(
        override val message: Message.Direct,
        override val user: User,
    ) : MessageRelation()

    fun isMime(account: Account): Boolean {
        return message.userId == User.Id(account.accountId, account.remoteId)
    }
}

sealed class MessageHistoryRelation : MessageRelation(){
    data class Group(
        override val message: Message,
        override val user: User,
        val group: GroupEntity
    ) : MessageHistoryRelation()

    data class Direct(
        override val message: Message,
        override val user: User,
        val recipient: User
    ) : MessageHistoryRelation()
}

suspend fun MessageRelation.toHistory(groupRepository: GroupRepository, userRepository: UserRepository): MessageHistoryRelation {
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