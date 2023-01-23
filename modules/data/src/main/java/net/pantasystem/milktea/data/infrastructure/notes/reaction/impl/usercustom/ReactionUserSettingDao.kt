package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionUserSettingDao{

    @Query("select * from reaction_user_setting where instance_domain = :instanceDomain order by weight asc")
    suspend fun findByInstanceDomain(instanceDomain: String): List<ReactionUserSetting>?

    @Query("select * from reaction_user_setting where instance_domain = :instanceDomain order by weight asc")
    fun observeByInstanceDomain(instanceDomain: String): Flow<List<ReactionUserSetting>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: ReactionUserSetting): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<ReactionUserSetting>): List<Long?>

    @Update
    suspend fun update(setting: ReactionUserSetting)

    @Update
    suspend fun updateAll(settings: List<ReactionUserSetting>)

    @Delete
    suspend fun delete(setting: ReactionUserSetting)

    @Delete
    suspend fun deleteAll(settings: List<ReactionUserSetting>)


}