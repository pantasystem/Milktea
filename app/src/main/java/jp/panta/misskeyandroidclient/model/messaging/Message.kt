package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.group.Group as GroupEntity
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*


sealed class Message{
    data class Id(
        val accountId: Long,
        val messageId: String
    )

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

    data class Group(
        override val id: Id,
        override val createdAt: Date,
        override val text: String?,
        override val userId: User.Id,
        override val fileId: String?,
        override val file: FileProperty?,
        override val isRead: Boolean,
        override val emojis: List<Emoji>,
        val groupId: String,
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
        val groupId: String,
        override val text: String?,
        override val fileId: String?
    ) : CreateMessage()
}

sealed class MessageRelation {

    abstract val message: Message

    data class Group(
        override val message: Message.Group,
        val group: GroupEntity,
        val user: User
    ) : MessageRelation()

    data class Direct(
        override val message: Message.Direct,
        val user: User,
        val recipient: User
    ) : MessageRelation()
}