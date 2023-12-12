package net.pantasystem.milktea.data.infrastructure.instance.ticker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InstanceTickerDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: InstanceTickerRecord): Long

    @Query("""
        SELECT * FROM instance_tickers
        WHERE uri = :host
        LIMIT 1
    """)
    suspend fun find(host: String): InstanceTickerRecord?

    @Query("""
        SELECT * FROM instance_tickers
        WHERE uri IN (:hosts)
    """)
    suspend fun findIn(hosts: List<String>): List<InstanceTickerRecord>

    @Query("""
        DELETE FROM instance_tickers
        WHERE uri = :host
    """)
    suspend fun delete(host: String): Int
}