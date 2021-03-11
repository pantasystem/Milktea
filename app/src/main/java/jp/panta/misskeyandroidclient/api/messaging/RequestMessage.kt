package jp.panta.misskeyandroidclient.api.messaging

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.serialization.Serializable

@Serializable
data class RequestMessage(
    val i: String,
    val userId: String?,
    val groupId: String?,
    val limit: Int?,
    val sinceId: String?,
    val untilId: String?,
    val markAsRead: Boolean?

){
    class Builder(private val account: Account, message: MessageDTO){
        val group = message.group
        val user = message.opponentUser(account)
        var limit: Int? = null
        var markAsRead: Boolean? = null

        fun build(sinceId: String?, untilId: String?, encryption: Encryption): RequestMessage {
            return RequestMessage(
                i = account.getI(encryption),
                userId = if(group == null) user?.id else null,
                groupId = group?.id,
                limit = limit,
                sinceId = sinceId,
                untilId = untilId,
                markAsRead = markAsRead
            )
        }
    }


}