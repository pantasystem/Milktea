package jp.panta.misskeyandroidclient.model.messaging

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.mfm.MFMParser
import jp.panta.misskeyandroidclient.mfm.Root
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.group.Group
import jp.panta.misskeyandroidclient.api.users.User
import java.io.Serializable
import java.util.*

data class Message(
    @SerializedName("id") val id: String,
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("text") val text: String?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("user") val user: User?,
    @SerializedName("recipientId") val recipientId: String?,
    @SerializedName("recipient") val recipient: User?,
    @SerializedName("groupId") val groupId: String?,
    @SerializedName("group") val group: Group?,
    @SerializedName("fileId") val fileId: String?,
    @SerializedName("file") val file: FileProperty?,
    @SerializedName("isRead") val isRead: Boolean?,
    val emojis: List<Emoji>?
): Serializable{
    fun isGroup(): Boolean{
        return group != null
    }

    fun opponentUser(account: Account) : User?{
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