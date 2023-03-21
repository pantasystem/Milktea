package net.pantasystem.milktea.api.misskey.messaging

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class MessageAction(
    @SerialName("i")
    val i: String?,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("groupId")
    val groupId: String? = null,

    @SerialName("text")
    val text: String? = null,

    @SerialName("fileId")
    val fileId: String? = null,

    @SerialName("messageId")
    val messageId: String? = null
): JavaSerializable