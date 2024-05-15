package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDAO {


    @Query(
        """
        SELECT * FROM notes
            WHERE id IN (:ids)
        """
    )
    @Transaction
    suspend fun getIn(ids: List<String>): List<NoteWithRelation>

    @Query(
        """
        SELECT * FROM notes
            WHERE id = :id
        """
    )
    @Transaction
    suspend fun get(id: String): NoteWithRelation?

    @Query(
        """
        SELECT * FROM notes
            WHERE reply_id = :replyId
            AND account_id = :accountId
        """
    )
    @Transaction
    suspend fun findByReplyId(accountId: Long, replyId: String): List<NoteWithRelation>

    // delete by id
    @Query(
        """
        DELETE FROM notes
            WHERE id = :id
        """
    )
    @Transaction
    suspend fun delete(id: String)

    // count
    @Query(
        """
        SELECT COUNT(*) FROM notes
            WHERE id = :id
        """
    )
    @Transaction
    suspend fun count(id: String): Int

    // flow by id
    @Query(
        """
        SELECT * FROM notes
            WHERE id = :id
        """
    )
    @Transaction
    fun observeById(id: String): Flow<NoteWithRelation?>

    @Query(
        """
        SELECT * FROM notes
            WHERE id IN (:ids)
        """
    )
    @Transaction
    fun observeByIds(ids: List<String>): Flow<List<NoteWithRelation>>

    // delete by user id
    @Query(
        """
        DELETE FROM notes
            WHERE user_id = :userId
            AND account_id = :accountId
        """
    )
    @Transaction
    suspend fun deleteByUserId(accountId: Long, userId: String): Int

    // clear
    @Query(
        """
        DELETE FROM notes
        """
    )
    @Transaction
    suspend fun clear()

    // count
    @Query(
        """
        SELECT COUNT(*) FROM notes
        """
    )
    @Transaction
    suspend fun count(): Long
}