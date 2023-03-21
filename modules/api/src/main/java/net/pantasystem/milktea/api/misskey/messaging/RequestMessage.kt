package net.pantasystem.milktea.api.misskey.messaging

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestMessage(
    @SerialName("i")
    val i: String,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("groupId")
    val groupId: String? = null,

    @SerialName("limit")
    val limit: Int? = 20,

    @SerialName("sinceId")
    val sinceId: String? = null,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("markAsRead")
    val markAsRead: Boolean? = null

)