package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "note_threads"
)
data class NoteThreadEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val accountId: Long,
    val targetNoteId: String,
)

// ancestors
// descendants
@Entity(
    tableName = "note_thread_ancestors",
    primaryKeys = ["thread_id", "note_id"]
)
data class NoteAncestorEntity(
    @ColumnInfo(
        name = "thread_id"
    )
    val threadId: String,

    @ColumnInfo(
        name = "note_id"
    )
    val noteId: String,
)

@Entity(
    tableName = "note_thread_descendants",
    primaryKeys = ["thread_id", "note_id"]
)
data class NoteDescendantEntity(
    @ColumnInfo(
        name = "thread_id"
    )
    val threadId: String,

    @ColumnInfo(
        name = "note_id"
    )
    val noteId: String,
)

data class NoteThreadWithRelation(
    @Embedded
    val thread: NoteThreadEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "thread_id"
    )
    val ancestors: List<NoteAncestorEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "thread_id"
    )
    val descendants: List<NoteDescendantEntity>,
)