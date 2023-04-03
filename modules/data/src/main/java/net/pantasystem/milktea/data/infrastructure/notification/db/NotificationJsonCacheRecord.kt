package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "notification_json_cache_v1",
    indices = [Index("key")],
    primaryKeys = ["accountId", "notificationId"]
)
data class NotificationJsonCacheRecord(
    @ColumnInfo(name = "accountId")
    val accountId: Long,

    @ColumnInfo(name = "notificationId")
    val notificationId: String,

    @ColumnInfo(name = "json")
    val json: String,

    @ColumnInfo(name = "key")
    val key: String?,

    @ColumnInfo(name = "weight")
    val weight: Int
)
