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
    abstract suspend fun findBy(uri: String): MastodonInstanceInfoRecord?

    @Query("""
        select * from mastodon_instance_info where uri = :uri
    """)
    abstract fun observeBy(uri: String): Flow<MastodonInstanceInfoRecord?>

}