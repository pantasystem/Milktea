package net.pantasystem.milktea.api.misskey.messaging

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class MessageAction(
    val i: String?,
    val userId: String? = null,
    val groupId: String? = null,
    val text: String? = null,
    val fileId: String? = null,
    val messageId: String? = null
): JavaSerializable