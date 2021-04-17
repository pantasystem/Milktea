package jp.panta.misskeyandroidclient.api.messaging

import jp.panta.misskeyandroidclient.mfm.MFMParser
import jp.panta.misskeyandroidclient.mfm.Root
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.api.groups.GroupDTO
import jp.panta.misskeyandroidclient.api.groups.toGroup
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
import java.util.Date
import jp.panta.misskeyandroidclient.model.group.Group as GroupEntity

@Serializable
data class MessageDTO(
    val id: String,
    @Serializable(with = DateSerializer::class) val createdAt: Date,
    val text: String? = null,
    val userId: String,
    val user: UserDTO,
    val recipientId: String? = null,
    val recipient: UserDTO? = null,
    val groupId: String? = null,
    val group: GroupDTO? = null,
    val fileId: String? = null,
    val file: FileProperty? = null,
    val isRead: Boolean,
    val emojis: List<Emoji>? = null
): JavaSerializable{
    fun isGroup(): Boolean{
        return group != null
    }



    val textNode: Root?
        get() {
            return MFMParser.parse(text, emojis)
        }
}

fun MessageDTO.entities(account: Account): Pair<Message, List<User>> {
    val list = mutableListOf<User>()
    val id = Message.Id(account.accountId, id)
    list.add(user.toUser(account, false))
    val message = if(groupId == null) {
        require(recipientId != null)
        Message.Direct(
            id,
            createdAt,
            text,
            User.Id(account.accountId, userId),
            fileId,
            file,
            isRead,
            emojis?: emptyList(),
            recipientId = User.Id(account.accountId, recipientId)
        )
    }else{
        Message.Group(
            id,
            createdAt,
            text,
            User.Id(account.accountId, userId),
            fileId,
            file,
            isRead,
            emojis?: emptyList(),
            GroupEntity.Id(account.accountId, groupId),
        )
    }
    return message to list
}