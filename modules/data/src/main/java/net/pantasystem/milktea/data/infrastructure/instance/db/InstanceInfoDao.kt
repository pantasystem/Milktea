package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanceInfoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(instanceInfoRecord: InstanceInfoRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(instanceInfoRecords: List<InstanceInfoRecord>): List<Long>

    @Query("delete from instance_info_v1_table")
    suspend fun clear()

    @Query("select * from instance_info_v1_table where id = :id")
    suspend fun findById(id: String): InstanceInfoRecord?

    @Query("select * from instance_info_v1_table")
    suspend fun findAll(): List<InstanceInfoRecord>

    @Query("select * from instance_info_v1_table where host = :host")
    suspend fun findByHost(host: String): InstanceInfoRecord?

    @Query("select * from instance_info_v1_table where host = :host")
    fun observeByHost(host: String): Flow<InstanceInfoRecord?>

    @Query("select * from instance_info_v1_table")
    fun observeAll(): Flow<List<InstanceInfoRecord>>
}