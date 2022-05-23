package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class EmojiAliasDAO {

    @Query("SELECT * FROM emoji_alias_table WHERE instanceDomain = :instanceDomain")
    abstract fun findByInstanceDomain(instanceDomain: String): List<EmojiAlias>

    @Query("SELECT * FROM emoji_alias_table WHERE instanceDomain = :instanceDomain AND name = :name")
    abstract fun findByEmoji(name: String, instanceDomain: String): List<EmojiAlias>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(alias: EmojiAlias)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(aliases: List<EmojiAlias>)
}