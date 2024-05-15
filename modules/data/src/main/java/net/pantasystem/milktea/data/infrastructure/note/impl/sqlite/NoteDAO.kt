package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

    // insert note
    @Transaction
    @Insert()
    suspend fun insert(note: NoteEntity)

    // insert notes
    @Transaction
    @Insert()
    suspend fun insertAll(notes: List<NoteEntity>)

    // update
    @Transaction
    @Update
    suspend fun update(note: NoteEntity)

    // insert reaction counts
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReactionCounts(reactionCounts: List<ReactionCountEntity>)

    // delete reaction by note id
    @Transaction
    @Query(
        """
        DELETE FROM reaction_counts
            WHERE note_id = :noteId
        """
    )
    suspend fun deleteReactionCountsByNoteId(noteId: String)

    // delete reaction counts
    @Transaction
    @Update
    suspend fun updateReactionCount(reactionCount: ReactionCountEntity)



    @Query(
        """
            DELETE FROM reaction_counts
                WHERE id IN (:ids)
        """
    )
    suspend fun deleteReactionCounts(ids: List<String>)

    // visible ids insert and delete
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisibleIds(visibleIds: List<NoteVisibleUserIdEntity>)


    // poll choices insert and delete
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPollChoices(pollChoices: List<NotePollChoiceEntity>)

    @Transaction
    @Query(
        """
        DELETE FROM note_poll_choices
            WHERE note_id = :noteId
        """
    )
    suspend fun deletePollChoicesByNoteId(noteId: String)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMastodonTags(tags: List<MastodonTagEntity>)

    // mastodon mentions insert
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMastodonMentions(mentions: List<MastodonMentionEntity>)

    // custom emojis insert and delete
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomEmojis(emojis: List<NoteCustomEmojiEntity>)

    @Transaction
    @Query(
        """
        DELETE FROM note_custom_emojis
            WHERE id IN (:ids)
        """
    )
    suspend fun deleteCustomEmojis(ids: List<String>)

    @Transaction
    @Query(
        """
        DELETE FROM note_custom_emojis
            WHERE note_id = :noteId
        """
    )
    suspend fun deleteCustomEmojisByNoteId(noteId: String)

    // note files
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteFiles(files: List<NoteFileEntity>)


}