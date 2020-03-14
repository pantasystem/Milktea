package jp.panta.misskeyandroidclient.model.core

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
abstract class ConnectionInformationDao{

    @Query("select * from connection_information")
    abstract fun findAll(): List<EncryptedConnectionInformation>

    @Insert
    abstract fun insert(connectionInformation: EncryptedConnectionInformation): Long

    fun add(connectionInformation: EncryptedConnectionInformation): Long{
        connectionInformation.updatedAt = Date()
        return insert(connectionInformation)
    }

    @Query("select * from connection_information where accountId = :id")
    abstract fun findByAccountId(id: String): List<EncryptedConnectionInformation>

    @Delete
    abstract fun delete(connectionInformation: EncryptedConnectionInformation)
}