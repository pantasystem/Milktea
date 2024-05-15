package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteThreadDAO {
    // insert, update, delete, select, relation用のinsert, deleteメソッドを追加

    @Insert
    suspend fun insert(noteThread: NoteThreadEntity)

    // update
    @Update
    suspend fun update(noteThread: NoteThreadEntity)

    // delete
    @Query(
        """
        DELETE FROM note_threads
            WHERE id = :id
        """
    )
    suspend fun delete(id: String)

    // attach ancestor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun attachAncestors(noteAncestor: List<NoteAncestorEntity>)

    // detach ancestor
    @Query(
        """
        DELETE FROM note_thread_ancestors
            WHERE thread_id = :threadId
        """
    )
    suspend fun detachAncestors(threadId: String)

    // attach descendant
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun attachDescendants(noteDescendant: List<NoteDescendantEntity>)

    // detach descendant
    @Query(
        """
        DELETE FROM note_thread_descendants
            WHERE thread_id = :threadId
        """
    )
    suspend fun detachDescendants(threadId: String)

    // select
    @Query(
        """
        SELECT * FROM note_threads
            WHERE id = :id
        """
    )
    suspend fun select(id: String): NoteThreadEntity?

    // select with relation
    @Query(
        """
        SELECT * FROM note_threads
            WHERE id = :id
        """
    )
    @Transaction
    suspend fun selectWithRelation(id: String): NoteThreadWithRelation?

    // coroutines flow
    @Query(
        """
        SELECT * FROM note_threads
            WHERE id = :id
        """
    )
    @Transaction
    fun observeWithRelation(id: String): Flow<NoteThreadWithRelation?>
}