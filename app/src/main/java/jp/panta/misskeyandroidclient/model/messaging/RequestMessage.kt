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
        val group: Group? = null
        val user = if(connectionInstance.userId == message.recipient?.id){
            message.user
        }else{
            message.recipient
        }
        var limit: Int? = null
        val markAsRead: Boolean? = null

        fun build(sinceId: String?, untilId: String?): RequestMessage{
            return RequestMessage(
                i = connectionInstance.getI()!!,
                userId = user?.id,
                groupId = group?.id,
                limit = limit,
                sinceId = sinceId,
                untilId = untilId,
                markAsRead = markAsRead
            )
        }
    }


}