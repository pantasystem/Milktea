package jp.panta.misskeyandroidclient.model.notification.db

import androidx.room.*
import jp.panta.misskeyandroidclient.model.notification.AccountNotificationCount

@Dao
abstract class UnreadNotificationDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(unreadNotification: UnreadNotification)

    @Delete
    abstract suspend fun delete(unreadNotification: UnreadNotification)

    @Query("SELECT un.accountId AS accountId, COUNT(un.notificationId) AS count FROM unread_notifications_table AS un GROUP BY un.accountId")
    abstract suspend fun countByAccount(): List<AccountNotificationCount>

    @Query("SELECT COUNT(*) FROM unread_notifications_table WHERE accountId = :accountId")
    abstract suspend fun countByAccountId(accountId: Long): Int

    @Query("SELECT * FROM unread_notifications_table WHERE accountId = :accountId")
    abstract suspend fun findByAccountId(accountId: Long): List<AccountNotificationCount>
}