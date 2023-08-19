package net.pantasystem.milktea.data.infrastructure.note.draft.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "poll_choice_table",
    primaryKeys = ["choice", "weight", "draft_note_id"],
    foreignKeys = [
        ForeignKey(
            childColumns = ["draft_note_id"],
            parentColumns = ["draft_note_id"],
            entity = DraftNoteDTO::class,
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("draft_note_id", "choice")]
)
data class PollChoiceDTO(
    @ColumnInfo(name = "choice")
    val choice: String,

    @ColumnInfo(name = "draft_note_id")
    val draftNoteId: Long,

    @ColumnInfo(name = "weight")
    val weight: Int
)