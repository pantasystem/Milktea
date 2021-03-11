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

    abstract val id: String
    abstract val createdAt: Date
    abstract val text: String?
    abstract val userId: User.Id
    abstract val fileId: String?
    abstract val file: FileProperty?
    abstract val isRead: Boolean
    abstract val emojis: List<Emoji>

    data class Group(
        override val id: String,
        override val createdAt: Date,
        override val text: String?,
        override val userId: User.Id,
        override val fileId: String?,
        override val file: FileProperty?,
        override val isRead: Boolean,
        override val emojis: List<Emoji>,
        val groupId: String,
        val group: GroupEntity
    ) : Message()

    /**
     * @param recipientId 受信者のUser.Id
     */
    data class Direct(
        override val id: String,
        override val createdAt: Date,
        override val text: String?,
        override val userId: User.Id,
        override val fileId: String?,
        override val file: FileProperty?,
        override val isRead: Boolean,
        override val emojis: List<Emoji>,
        val recipientId: User.Id
    ) : Message()
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