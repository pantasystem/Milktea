package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MetaDAO{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(meta: MetaDTO): Long

    @Delete
    abstract fun delete(meta: MetaDTO)

    @Update
    abstract fun update(meta: MetaDTO)

    @Transaction
    @Query("select * from meta_table where uri = :instanceDomain")
    abstract fun findByInstanceDomain(instanceDomain: String): MetaRelation?

    @Transaction
    @Query("select * from meta_table where uri = :instanceDomain")
    abstract fun observeByInstanceDomain(instanceDomain: String): Flow<MetaRelation?>

}