package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.Entity

@Entity(
    tableName = "note_threads"
)
data class NoteThread(
    val id: String,
    val accountId: Long,
    val targetNoteId: String,
)

// ancestors
// descendants
