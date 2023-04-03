package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MastodonInstanceInfoDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(instanceInfo: MastodonInstanceInfoRecord)

    @Update
    abstract suspend fun update(instanceInfo: MastodonInstanceInfoRecord)

    @Delete
    abstract suspend fun delete(instanceInfo: MastodonInstanceInfoRecord)

    @Query("""
        select * from mastodon_instance_info where uri = :uri
    """)
    @Transaction
    abstract suspend fun findBy(uri: String): MastodonInstanceInfoRelated?

    @Query("""
        select * from mastodon_instance_info where uri = :uri
    """)
    @Transaction
    abstract fun observeBy(uri: String): Flow<MastodonInstanceInfoRelated?>

    @Query("""
        delete from mastodon_instance_fedibird_capabilities where uri = :uri
    """)
    abstract fun clearFedibirdCapabilities(uri: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertFedibirdCapabilities(list: List<FedibirdCapabilitiesRecord>): List<Long>

}