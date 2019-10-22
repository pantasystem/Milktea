package jp.panta.misskeyandroidclient.model.auth

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ConnectionInstanceDao{
    @Query("select * from connection_instance")
    fun findAll(): List<ConnectionInstance>?

    @Query("select * from connection_instance where userId = :userId LIMIT 1")
    fun findByUserId(userId: String): ConnectionInstance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(connectionInstance: ConnectionInstance)

    @Delete
    fun delete(connectionInstance: ConnectionInstance)
}