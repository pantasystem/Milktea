package net.pantasystem.milktea.model.notification

import android.os.Bundle
import net.pantasystem.milktea.common.runCancellableCatching


data class PushNotification(
    val notificationId: String,
    val type: String,
    val body: String,
    val title: String,
    val accountId: Long,
    val noteId: String?,
    val userId: String?,
) {
    fun isNearUserNotification(): Boolean {
        return userId != null
                && (type == "follow"
                || type == "receiveFollowRequest"
                || type == "followRequestAccepted")
    }

    fun isNearNoteNotification(): Boolean {
        return noteId != null
                && (type == "mention"
                || type == "reply"
                || type == "renote"
                || type == "quote"
                || type == "reaction")
    }
}

fun Map<String, String?>.toPushNotification(): PushNotification {
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

fun Bundle.toPushNotification(): Result<PushNotification> = runCancellableCatching {
    listOf(
        "title" to getString("title"),
        "body" to getString("body"),
        "notificationId" to getString("notificationId"),
        "type" to getString("type"),
        "accountId" to getString("accountId"),
        "userId" to  getString("userId"),
        "noteId" to getString("noteId"),
    ).toMap().toPushNotification()
}