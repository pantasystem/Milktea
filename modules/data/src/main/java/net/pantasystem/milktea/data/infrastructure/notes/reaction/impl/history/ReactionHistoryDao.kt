package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionHistoryDao{

    @Query("select * from reaction_history")
    fun findAll() : List<ReactionHistoryRecord>?

    @Query("select reaction, count(reaction) as reaction_count from reaction_history where instance_domain=:instanceDomain group by reaction order by reaction_count desc")
    fun sumReactions(instanceDomain: String) : List<ReactionHistoryCountRecord>

    @Query("select reaction, count(reaction) as reaction_count from reaction_history where instance_domain=:instanceDomain group by reaction order by reaction_count desc")
    fun observeSumReactions(instanceDomain: String) : Flow<List<ReactionHistoryCountRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reactionHistory: ReactionHistoryRecord)
}

