package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
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
    class Builder(private val connectionInstance: ConnectionInstance, message: Message){
        val group = message.group
        val user = message.opponentUser(connectionInstance)
        var limit: Int? = null
        var markAsRead: Boolean? = null

        fun build(sinceId: String?, untilId: String?): RequestMessage{
            return RequestMessage(
                i = connectionInstance.getI()!!,
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