package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionHistoryDao{

    @Query("select * from reaction_history")
    suspend fun findAll() : List<ReactionHistoryRecord>?

    @Query("select reaction, count(reaction) as reaction_count from reaction_history where instance_domain=:instanceDomain group by reaction order by reaction_count desc limit :limit")
    suspend fun sumReactions(instanceDomain: String, limit: Int) : List<ReactionHistoryCountRecord>

    @Query("select reaction, count(reaction) as reaction_count from reaction_history where instance_domain=:instanceDomain group by reaction order by reaction_count desc limit :limit")
    fun observeSumReactions(instanceDomain: String, limit: Int) : Flow<List<ReactionHistoryCountRecord>>
    
    @Query("select * from reaction_history where instance_domain=:instanceDomain order by id desc limit :limit")
    fun observeRecentlyUsed(instanceDomain: String, limit: Int): Flow<List<ReactionHistoryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reactionHistory: ReactionHistoryRecord)
}

