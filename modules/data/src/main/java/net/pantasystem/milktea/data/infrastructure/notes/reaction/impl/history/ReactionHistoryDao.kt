package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionHistoryDao {

    @Query("select * from reaction_history")
    suspend fun findAll(): List<ReactionHistoryRecord>?

    @Query("select reaction, count(reaction) as reaction_count from reaction_history where instance_domain=:instanceDomain group by reaction order by reaction_count desc limit :limit")
    suspend fun sumReactions(instanceDomain: String, limit: Int): List<ReactionHistoryCountRecord>

    @Query("select reaction, count(reaction) as reaction_count from reaction_history where instance_domain=:instanceDomain group by reaction order by reaction_count desc limit :limit")
    fun observeSumReactions(
        instanceDomain: String,
        limit: Int
    ): Flow<List<ReactionHistoryCountRecord>>

    @Query("""
        select * from reaction_history as r1 
            where exists(
                select 1 from(
                    select max(id) as id from reaction_history 
                        where instance_domain = :instanceDomain group by reaction, instance_domain
                ) r2 where r1.id = r2.id
            ) 
        order by id desc limit :limit
    """)
    fun observeRecentlyUsed(instanceDomain: String, limit: Int): Flow<List<ReactionHistoryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reactionHistory: ReactionHistoryRecord)


    @Query(
        """
            select 
                r1.accountId as accountId, 
                r1.target_user_id as targetUserId,
                count(r1.id) as reactionCount
            from reaction_history as r1
                inner join user as u 
                    on r1.accountId = u.accountId and r1.target_user_id = u.serverId
                inner join user_related_state as ur
                    on u.id = ur.userId
                where ur.isFollowing = 0
                    and r1.accountId = :accountId
                group by r1.accountId, r1.target_user_id
                    order by count(r1.id) desc
                limit 100
        """
    )
    suspend fun findFrequentlyReactionUserAndUnFollowed(accountId: Long): List<FrequentlyReactionAndUnFollowedUserRecord>
}

