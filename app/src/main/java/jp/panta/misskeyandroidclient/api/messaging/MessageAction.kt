package jp.panta.misskeyandroidclient.api.messaging

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class MessageAction(
    val i: String?,
    val userId: String?,
    val groupId: String?,
    val text: String?,
    val fileId: String?,
    val messageId: String?
): JavaSerializable