package net.pantasystem.milktea.data.infrastructure.note.reaction.impl

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionAuthorDAO {
    @Transaction
    @Query("""
        SELECT * FROM reaction_authors WHERE id = :id
    """)
    suspend fun findById(id: String): ReactionAuthorWithUsers?

    @Upsert
    suspend fun upsert(entity: ReactionAuthorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun appendReactionUsers(users: List<ReactionUserEntity>): List<Long>

    @Query("""
        DELETE FROM reaction_authors WHERE id = :id
    """)
    suspend fun remove(id: String)

    @Transaction
    @Query("""
        SELECT * FROM reaction_authors WHERE id = :id
    """)
    fun observeById(id: String): Flow<ReactionAuthorWithUsers?>
}