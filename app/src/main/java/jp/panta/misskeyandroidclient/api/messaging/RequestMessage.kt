package jp.panta.misskeyandroidclient.api.messaging

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.serialization.Serializable

@Serializable
data class RequestMessage(
    val i: String,
    val userId: String? = null,
    val groupId: String? = null,
    val limit: Int? = 20,
    val sinceId: String? = null,
    val untilId: String? = null,
    val markAsRead: Boolean? = null

)