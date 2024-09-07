package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction

/**
 * キャッシュの塊を表すエンティティ
 */
@Entity(
    tableName = "notification_timelines"
)
data class NotificationTimelineEntity(
    val accountId: Long,
    
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)


// exclude types
@Entity(
    tableName = "notification_timeline_excluded_types",
    primaryKeys = ["timelineId", "type"]
)
data class NotificationTimelineExcludedTypeEntity(
    val timelineId: Long,
    val type: String,
)

// include types
@Entity(
    tableName = "notification_timeline_included_types",
    primaryKeys = ["timelineId", "type"]
)
data class NotificationTimelineIncludedTypeEntity(
    val timelineId: Long,
    val type: String,
)

// relation
data class NotificationTimelineRelation(
    @Embedded
    val timeline: NotificationTimelineEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId"
    )
    val excludedTypes: List<NotificationTimelineExcludedTypeEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId"
    )
    val includedTypes: List<NotificationTimelineIncludedTypeEntity>,
)

@Entity(
    tableName = "notification_timeline_items",
    primaryKeys = ["timelineId", "notificationId"],
    indices = [
        androidx.room.Index("timelineId"),
    ]
)
data class NotificationTimelineItemEntity(
    val timelineId: Long,
    val notificationId: String,
    val cachedAt: Long,
)

@Dao
interface NotificationTimelineDAO {

    // insert
    @Insert
    suspend fun insert(entity: NotificationTimelineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExcludedTypes(entity: List<NotificationTimelineExcludedTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncludedTypes(entity: List<NotificationTimelineIncludedTypeEntity>)


    @Query(
        """
            SELECT * FROM notification_timelines
            JOIN (
                SELECT notification_timeline_excluded_types.timelineId FROM notification_timeline_excluded_types
                WHERE type IN (:excludeTypes)
                GROUP BY notification_timeline_excluded_types.timelineId
                HAVING COUNT(notification_timeline_excluded_types.timelineId) = COUNT(:excludeTypesCount)
            ) as excludes ON notification_timelines.id = excludes.timelineId
            JOIN (
                SELECT notification_timeline_included_types.timelineId FROM notification_timeline_included_types
                WHERE type IN (:includeTypes)
                GROUP BY notification_timeline_included_types.timelineId
                HAVING COUNT(notification_timeline_included_types.timelineId) = COUNT(:includeTypesCount)
            ) as includes ON notification_timelines.id = includes.timelineId
            WHERE accountId = :accountId
        """
    )
    suspend fun findByExcludeTypesAndIncludeTypes(
        accountId: Long,
        excludeTypes: List<String>,
        includeTypes: List<String>,
        excludeTypesCount: Int = excludeTypes.size,
        includeTypesCount: Int = includeTypes.size
    ): List<NotificationTimelineRelation>

    // リレーションが何一つ設定されていないタイムラインを取得
    @Query(
        """
            SELECT * FROM notification_timelines
            WHERE id NOT IN (
                SELECT timelineId FROM notification_timeline_excluded_types
            ) AND id NOT IN (
                SELECT timelineId FROM notification_timeline_included_types
            )
            AND accountId = :accountId
        """
    )
    suspend fun findEmpty(accountId: Long): List<NotificationTimelineRelation>

    // include typesに完全一致し、exclude typesのリレーションが存在しないタイムラインを取得
    @Query(
        """
            SELECT * FROM notification_timelines
            JOIN (
                SELECT notification_timeline_included_types.timelineId FROM notification_timeline_included_types
                WHERE type IN (:includeTypes)
                GROUP BY notification_timeline_included_types.timelineId
                HAVING COUNT(notification_timeline_included_types.timelineId) = COUNT(:includeTypesCount)
            ) as includes ON notification_timelines.id = includes.timelineId
            WHERE id NOT IN (
                SELECT timelineId FROM notification_timeline_excluded_types
            )
            AND accountId = :accountId
        """
    )
    suspend fun findByIncludeTypes(accountId: Long, includeTypes: List<String>, includeTypesCount: Int = includeTypes.size): List<NotificationTimelineRelation>

    // exclude typesに完全一致し、include typesのリレーションが存在しないタイムラインを取得
    @Query(
        """
            SELECT * FROM notification_timelines
            JOIN (
                SELECT notification_timeline_excluded_types.timelineId FROM notification_timeline_excluded_types
                WHERE type IN (:excludeTypes)
                GROUP BY notification_timeline_excluded_types.timelineId
                HAVING COUNT(notification_timeline_excluded_types.timelineId) = COUNT(:excludeTypesCount)
            ) as excludes ON notification_timelines.id = excludes.timelineId
            WHERE id NOT IN (
                SELECT timelineId FROM notification_timeline_included_types
            )
            AND accountId = :accountId
        """
    )
    suspend fun findByExcludeTypes(accountId: Long, excludeTypes: List<String>, excludeTypesCount: Int = excludeTypes.size): List<NotificationTimelineRelation>

    // find by id
    @Query(
        """
            SELECT * FROM notification_timelines
            WHERE id = :id
        """
    )
    suspend fun findById(id: Long): NotificationTimelineRelation?

    // find notifications join notification_timeline_items
    @Query(
        """
            SELECT notifications.* FROM notification_timeline_items
            JOIN notifications ON notification_timeline_items.notificationId = notifications.id
            WHERE timelineId = :timelineId
            ORDER BY notifications.notification_id DESC
            LIMIT :limit
        """
    )
    suspend fun findNotifications(timelineId: Long, limit: Int): List<NotificationWithDetails>

    // find notifications join notification_timeline_items and untilId
    @Query(
        """
            SELECT notifications.* FROM notification_timeline_items
            JOIN notifications ON notification_timeline_items.notificationId = notifications.id
            WHERE timelineId = :timelineId AND notifications.notification_id < :untilId
            ORDER BY notifications.notification_id DESC
            LIMIT :limit
        """
    )
    suspend fun findNotificationsUntilId(timelineId: Long, untilId: String, limit: Int): List<NotificationWithDetails>

    @Query(
        """
            SELECT notifications.* FROM notification_timeline_items
            JOIN notifications ON notification_timeline_items.notificationId = notifications.id
            WHERE timelineId = :timelineId AND notifications.notification_id > :sinceId
            ORDER BY notifications.notification_id DESC
            LIMIT :limit
        """
    )
    @Transaction
    suspend fun findNotificationsSinceId(timelineId: Long, sinceId: String, limit: Int): List<NotificationWithDetails>

    // insert notification_timeline_items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationItems(entity: List<NotificationTimelineItemEntity>)


}