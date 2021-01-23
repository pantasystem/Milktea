package jp.panta.misskeyandroidclient.model.notes.reaction.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistory
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryCount

@Dao
interface ReactionHistoryDao{

    @Query("select * from reaction_history")
    fun findAll() : List<ReactionHistory>?

    @Query("select reaction, count(reaction) as reaction_count from reaction_history where instance_domain=:instanceDomain group by reaction order by reaction_count desc")
    fun sumReactions(instanceDomain: String) : List<ReactionHistoryCount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reactionHistory: ReactionHistory)
}