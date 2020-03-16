package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.group.Group

data class RequestMessage(
    val i: String,
    val userId: String?,
    val groupId: String?,
    val limit: Int?,
    val sinceId: String?,
    val untilId: String?,
    val markAsRead: Boolean?

){
    class Builder(private val accountRelation: AccountRelation, message: Message){
        val group = message.group
        val user = message.opponentUser(accountRelation.account)
        var limit: Int? = null
        var markAsRead: Boolean? = null

        fun build(sinceId: String?, untilId: String?, encryption: Encryption): RequestMessage{
            return RequestMessage(
                i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
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