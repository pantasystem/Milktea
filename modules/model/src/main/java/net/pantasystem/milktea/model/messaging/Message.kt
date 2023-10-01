package net.pantasystem.milktea.model.messaging

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.group.Group as GroupEntity


sealed class Message {
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
    abstract val emojis: List<CustomEmoji>

    /**
     * isReadをtrueにして新しいオブジェクトを返す
     */
    abstract fun read(): Message

    fun messagingId(account: Account): MessagingId {
        return when (this) {
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
        override val emojis: List<CustomEmoji>,
        val groupId: GroupEntity.Id,
        val reads: List<User.Id>
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
        override val emojis: List<CustomEmoji>,
        val recipientId: User.Id
    ) : Message() {

        companion object;

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

fun Message.Direct.Companion.make(
    id: Message.Id,
    userId: User.Id,
    recipientId: User.Id,
    createdAt: Instant = Clock.System.now(),
    text: String? = null,
    fileId: String? = null,
    file: FileProperty? = null,
    isRead: Boolean = false,
    emojis: List<CustomEmoji> = emptyList()
): Message.Direct {

    return Message.Direct(
        id = id,
        userId = userId,
        recipientId = recipientId,
        createdAt = createdAt,
        text = text,
        file = file,
        fileId = fileId,
        isRead = isRead,
        emojis = emojis,
    )
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

        fun create(messagingId: MessagingId, text: String?, fileId: String?): CreateMessage {
            return when (messagingId) {
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
    abstract val account: Account

    data class Group(
        override val message: Message.Group,
        override val user: User,
        override val account: Account
    ) : MessageRelation()

    data class Direct(
        override val message: Message.Direct,
        override val user: User,
        override val account: Account
    ) : MessageRelation()

    fun isMime(account: Account): Boolean {
        return message.userId == User.Id(account.accountId, account.remoteId)
    }

    fun isMine(): Boolean {
        return message.userId == User.Id(account.accountId, account.remoteId)
    }

}

sealed class MessageHistoryRelation : MessageRelation() {
    data class Group(
        override val message: Message,
        override val user: User,
        val group: GroupEntity,
        override val account: Account
    ) : MessageHistoryRelation()

    data class Direct(
        override val message: Message,
        override val user: User,
        val recipient: User,
        override val account: Account
    ) : MessageHistoryRelation()
}

val MessageHistoryRelation.Direct.partner: User
    get() {
        return if (this.recipient.id.id == account.remoteId) {
            this.user
        } else {
            this.recipient
        }
    }

val MessageHistoryRelation.thumbnailUrl: String?
    get() {
        return when (this) {
            is MessageHistoryRelation.Direct -> partner.avatarUrl
            is MessageHistoryRelation.Group -> user.avatarUrl
        }
    }

val MessageHistoryRelation.messagingId: MessagingId
    get() {
        return message.messagingId(account)
    }


fun MessageHistoryRelation.getTitle(isUserNameDefault: Boolean): String {

    return when (this) {
        is MessageHistoryRelation.Direct -> {
            if (isUserNameDefault) {
                partner.displayUserName
            } else {
                partner.displayName
            }
        }
        is MessageHistoryRelation.Group -> {
            group.name
        }
    }
}

suspend fun MessageRelation.toHistory(
    groupRepository: GroupRepository,
    userRepository: UserRepository
): MessageHistoryRelation {
    return when (val msg = message) {
        is Message.Direct -> {
            val recipient = userRepository.find(msg.recipientId, false)
            MessageHistoryRelation.Direct(this.message, this.user, recipient, account)
        }
        is Message.Group -> {
            val group = groupRepository.syncOne(msg.groupId)
            MessageHistoryRelation.Group(this.message, this.user, group, account)
        }
    }

}