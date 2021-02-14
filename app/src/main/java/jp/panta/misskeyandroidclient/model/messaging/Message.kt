package jp.panta.misskeyandroidclient.model.messaging

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.mfm.MFMParser
import jp.panta.misskeyandroidclient.mfm.Root
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.group.Group
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
import java.util.Date

@Serializable
data class Message(
    val id: String,
    @Serializable(with = DateSerializer::class) val createdAt: Date,
    val text: String?,
    val userId: String?,
    val user: UserDTO?,
    val recipientId: String?,
    val recipient: UserDTO?,
    val groupId: String?,
    val group: Group?,
    val fileId: String?,
    val file: FileProperty?,
    val isRead: Boolean?,
    val emojis: List<Emoji>?
): JavaSerializable{
    fun isGroup(): Boolean{
        return group != null
    }

    fun opponentUser(account: Account) : UserDTO?{
        return if(recipient?.id == account.remoteId){
            user
        }else{
            recipient
        }
    }

    fun messagingId(account: Account): MessagingId {
        return MessagingId(
            this,
            account
        )
    }

    val textNode: Root?
        get() {
            return MFMParser.parse(text, emojis)
        }
}