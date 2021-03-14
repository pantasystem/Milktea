package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.EntityId
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.group.Group as GroupEntity
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*


sealed class Message{
    data class Id(
        val accountId: Long,
        val messageId: String
    ) : EntityId

    abstract val id: Id
    abstract val createdAt: Date
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
        override val createdAt: Date,
        override val text: String?,
        override val userId: User.Id,
        override val fileId: String?,
        override val file: FileProperty?,
        override val isRead: Boolean,
        override val emojis: List<Emoji>,
        val groupId: GroupEntity.Id,
        val group: GroupEntity
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
        override val createdAt: Date,
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
}

sealed class MessageRelation {

    abstract val message: Message
    abstract val user: User

    data class Group(
        override val message: Message.Group,
        val group: GroupEntity,
        override val user: User
    ) : MessageRelation()

    data class Direct(
        override val message: Message.Direct,
        override val user: User,
        val recipient: User
    ) : MessageRelation() {
        fun opponentUser(account: Account) : User{
            return if(recipient.id == User.Id(account.accountId, account.remoteId)){
                user
            }else{
                recipient
            }
        }
    }

    fun isMime(account: Account): Boolean {
        return message.userId == User.Id(account.accountId, account.remoteId)
    }
}