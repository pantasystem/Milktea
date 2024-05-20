package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface NotificationCacheDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowNotifications(items: List<FollowNotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteNotifications(items: List<NoteNotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReactionNotifications(items: List<ReactionNotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPollVoteNotifications(items: List<PollVoteNotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupInvitedNotifications(items: List<GroupInvitedNotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnknownNotifications(items: List<UnknownNotificationEntity>)

    @Query(
        """
            select * from notifications
                where account_id = :accountId
                order by notification_id desc
                limit :limit
        """
    )
    @Transaction
    suspend fun findNotifications(accountId: Long, limit: Int): List<NotificationWithDetails>

    @Query(
        """
            select * from notifications
                where account_id = :accountId and notification_id < :untilId
                order by notification_id desc
                limit :limit
        """
    )
    @Transaction
    suspend fun findNotificationsByUntilId(accountId: Long, untilId: String, limit: Int): List<NotificationWithDetails>

    @Query(
        """
            select * from notifications
                where account_id = :accountId and notification_id > :sinceId
                order by notification_id desc
                limit :limit
        """
    )
    @Transaction
    suspend fun findNotificationsBySinceId(accountId: Long, sinceId: String, limit: Int): List<NotificationWithDetails>
}