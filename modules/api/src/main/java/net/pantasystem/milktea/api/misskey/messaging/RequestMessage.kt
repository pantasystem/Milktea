package net.pantasystem.milktea.api.misskey.messaging

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