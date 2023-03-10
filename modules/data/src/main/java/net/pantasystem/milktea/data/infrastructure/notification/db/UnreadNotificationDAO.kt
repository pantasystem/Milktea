package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UnreadNotificationDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(unreadNotification: UnreadNotification)

    @Query("DELETE FROM unread_notifications_table WHERE accountId = :accountId AND notificationId = :notificationId")
    abstract suspend fun delete(accountId: Long, notificationId: String)



    @Query("SELECT un.accountId AS accountId, COUNT(un.notificationId) AS count FROM unread_notifications_table AS un GROUP BY un.accountId")
    abstract fun countByAccount(): Flow<List<AccountNotificationCount>>



    @Query("SELECT COUNT(*) FROM unread_notifications_table WHERE accountId = :accountId")
    abstract fun countByAccountId(accountId: Long) : Flow<Int>

    @Query("DELETE FROM unread_notifications_table WHERE accountId = :accountId")
    abstract suspend fun deleteWhereAccountId(accountId: Long)

    @Query("""
        SELECT * FROM unread_notifications_table
            WHERE accountId = :accountId
            ORDER BY notificationId DESC
            LIMIT 1
    """)
    abstract suspend fun getLatestUnreadId(accountId: Long): UnreadNotification?
}