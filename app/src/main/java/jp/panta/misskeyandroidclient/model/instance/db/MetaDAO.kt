package jp.panta.misskeyandroidclient.model.instance.db

import androidx.room.*
import jp.panta.misskeyandroidclient.model.instance.Meta

@Dao
abstract class MetaDAO{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(meta: MetaDTO)

    @Delete
    abstract fun delete(meta: MetaDTO)

    @Transaction
    @Query("select * from meta_table where uri = :instanceDomain")
    abstract fun findByInstanceDomain(instanceDomain: String): MetaRelation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(emojiDTO: EmojiDTO)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(emojis: List<EmojiDTO>)

    @Query("select * from emoji_table where name = :name and instanceDomain = :instanceDomain")
    abstract fun findByNameAndInstanceDomain(name: String, instanceDomain: String) : EmojiDTO

    @Query("select * from emoji_table where instanceDomain = :instanceDomain")
    abstract fun findAllByInstanceDomain(instanceDomain: String) : List<EmojiDTO>

}