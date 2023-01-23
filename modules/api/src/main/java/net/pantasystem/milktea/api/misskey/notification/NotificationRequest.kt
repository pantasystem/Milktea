package net.pantasystem.milktea.api.misskey.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest (
    val i: String,
    val limit: Int? = null,
    val sinceId: String? = null,
    val untilId: String? = null,
    val following: Boolean? = null,
    val markAsRead: Boolean? = null,
    val includeTypes: List<String>? = null,
    val excludeTypes: List<String>? = null
    )