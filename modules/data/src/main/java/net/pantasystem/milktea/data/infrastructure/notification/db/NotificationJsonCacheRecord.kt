package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "notification_json_cache_v1",
    indices = [Index("key")],
    primaryKeys = ["accountId", "notificationId"]
)
data class NotificationJsonCacheRecord(
    val accountId: Long,
    val notificationId: String,
    val json: String,
    val key: String?,
    val weight: Int
)
