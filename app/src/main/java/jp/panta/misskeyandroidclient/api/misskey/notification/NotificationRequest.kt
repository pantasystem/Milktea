package jp.panta.misskeyandroidclient.api.misskey.notification

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