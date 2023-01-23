package net.pantasystem.milktea.data.infrastructure.nodeinfo.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeInfoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(nodeInfo: NodeInfoRecord): Long

    @Update
    suspend fun update(nodeInfo: NodeInfoRecord)

    @Delete
    suspend fun delete(nodeInfo: NodeInfoRecord)

    @Query("select * from nodeinfo where host = :host limit 1")
    fun observe(host: String): Flow<NodeInfoRecord?>

    @Query("select * from nodeinfo where host = :host limit 1")
    suspend fun find(host: String): NodeInfoRecord?

    @Query("select * from nodeinfo")
    suspend fun findAll(): List<NodeInfoRecord>
}