package net.pantasystem.milktea.api.misskey.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    @SerialName("i")
    val i: String,

    @SerialName("limit")
    val limit: Int? = null,

    @SerialName("sinceId")
    val sinceId: String? = null,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("following")
    val following: Boolean? = null,

    @SerialName("markAsRead")
    val markAsRead: Boolean? = null,

    @SerialName("includeTypes")
    val includeTypes: List<String>? = null,

    @SerialName("excludeTypes")
    val excludeTypes: List<String>? = null,
)