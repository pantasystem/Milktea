package net.pantasystem.milktea.data.infrastructure.note.timeline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TimelineCacheDAO {

    @Query(
        """
        SELECT * FROM timeline_item
        WHERE account_id = :accountId
        AND page_id = :pageId
        ORDER BY note_id DESC
        LIMIT :limit
        """
    )
    suspend fun getTimelineItems(
        accountId: Long,
        pageId: Long,
        limit: Int,
    ): List<TimelineItemEntity>

    // sinceIdで取得した場合
    @Query(
        """
        SELECT * FROM timeline_item
        WHERE account_id = :accountId
        AND page_id = :pageId
        AND note_id > :sinceId
        ORDER BY note_id DESC
        LIMIT :limit
        """
    )
    suspend fun getTimelineItemsSinceId(
        accountId: Long,
        pageId: Long,
        sinceId: String,
        limit: Int,
    ): List<TimelineItemEntity>

    // untilIdで取得した場合
    @Query(
        """
        SELECT * FROM timeline_item
        WHERE account_id = :accountId
        AND page_id = :pageId
        AND note_id < :untilId
        ORDER BY note_id DESC
        LIMIT :limit
        """
    )
    suspend fun getTimelineItemsUntilId(
        accountId: Long,
        pageId: Long,
        untilId: String,
        limit: Int,
    ): List<TimelineItemEntity>

    // untilIdとsinceIdで取得した場合
    @Query(
        """
        SELECT * FROM timeline_item
        WHERE account_id = :accountId
        AND page_id = :pageId
        AND note_id < :untilId
        AND note_id > :sinceId
        ORDER BY note_id DESC
        LIMIT :limit
        """
    )
    suspend fun getTimelineItemsUntilIdAndSinceId(
        accountId: Long,
        pageId: Long,
        untilId: String,
        sinceId: String,
        limit: Int,
    ): List<TimelineItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TimelineItemEntity>)

    // clear
    @Query(
        """
        DELETE FROM timeline_item
        WHERE account_id = :accountId
        AND page_id = :pageId
        """
    )
    suspend fun clear(accountId: Long, pageId: Long)
}