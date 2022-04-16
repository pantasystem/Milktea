package net.pantasystem.milktea.data.model.notes.reaction.usercustom

import androidx.room.*

@Dao
interface ReactionUserSettingDao{

    @Query("select * from reaction_user_setting where instance_domain = :instanceDomain order by weight asc")
    fun findByInstanceDomain(instanceDomain: String): List<ReactionUserSetting>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(setting: ReactionUserSetting): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(settings: List<ReactionUserSetting>): List<Long?>

    @Update
    fun update(setting: ReactionUserSetting)

    @Update
    fun updateAll(settings: List<ReactionUserSetting>)

    @Delete
    fun delete(setting: ReactionUserSetting)

    @Delete
    fun deleteAll(settings: List<ReactionUserSetting>)


}