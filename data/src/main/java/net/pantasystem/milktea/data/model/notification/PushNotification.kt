package net.pantasystem.milktea.data.model.notification


data class PushNotification(
    val notificationId: String,
    val type: String,
    val body: String,
    val title: String,
    val accountId: Long,
    val noteId: String?,
    val userId: String?,
)

fun Map<String, String>.toPushNotification(): PushNotification {
    val title = this["title"]
    val body = this["body"]
    val notificationId = this["notificationId"]
    val type = this["type"]
    val accountId = this["accountId"]
    val userId = this["userId"]
    val noteId = this["noteId"]
    if(title == null || body == null || notificationId == null || type == null || accountId == null) {
        throw IllegalArgumentException(
            "title, body, notificationId, type, accountIdいずれかの必要な項目が足りません data:${this}"
        )
    }
    return PushNotification(
        notificationId = notificationId,
        type = type,
        body = body,
        title = title,
        accountId = accountId.toLong(),
        noteId = noteId,
        userId = userId
    )
}