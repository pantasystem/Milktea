package net.pantasystem.milktea.data.infrastructure.drive

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DriveFileRecordDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(record: DriveFileRecord)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAll(records: List<DriveFileRecord>): List<Long>

    @Update
    abstract suspend fun update(record: DriveFileRecord)

    @Query("select * from drive_file_v1 where relatedAccountId=:accountId and serverId=:id")
    abstract suspend fun findOne(accountId: Long, id: String): DriveFileRecord?

    @Query("delete from drive_file_v1 where relatedAccountId=:accountId and serverId=:id")
    abstract suspend fun delete(accountId: Long, id: String)

    @Query("select * from drive_file_v1 where relatedAccountId=:accountId and serverId in (:serverIds)")
    abstract fun observeIn(accountId: Long, serverIds: List<String>): Flow<List<DriveFileRecord>>

    @Query("select * from drive_file_v1 where relatedAccountId=:accountId and serverId=:id")
    abstract fun observe(accountId: Long, id: String): Flow<DriveFileRecord?>

    @Query("select * from drive_file_v1 where relatedAccountId=:accountId and serverId in (:serverIds)")
    abstract suspend fun findIn(accountId: Long, serverIds: List<String>): List<DriveFileRecord>

    @Query(
        """
        delete from drive_file_v1
            where not exists(
                select * from draft_file_v2_table as draft 
                    where draft.filePropertyId = drive_file_v1.id
            )
        """
    )
    abstract suspend fun deleteUnUsedFiles()

    @Query("select count(*) from drive_file_v1")
    abstract suspend fun count(): Int

}