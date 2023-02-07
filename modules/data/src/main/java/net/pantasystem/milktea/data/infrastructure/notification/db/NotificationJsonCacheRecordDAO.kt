package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NotificationJsonCacheRecordDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NotificationJsonCacheRecord>)

    @Query(
        """
            select * from notification_json_cache_v1 
                where accountId = :accountId and `key` = :key order by weight asc
        """
    )
    suspend fun findByKey(accountId: Long, key: String): List<NotificationJsonCacheRecord>

    @Query(
        """
            select * from notification_json_cache_v1 
                where accountId = :accountId and `key` is null order by weight asc
        """
    )
    suspend fun findByNullKey(accountId: Long): List<NotificationJsonCacheRecord>

    @Query(
        """
            delete from notification_json_cache_v1
                where accountId = :accountId and `key` = :key
        """
    )
    suspend fun deleteByKey(accountId: Long, key: String)

    @Query(
        """
            delete from notification_json_cache_v1
                where accountId = :accountId and `key` is null
        """
    )
    suspend fun deleteByNullKey(accountId: Long)
}