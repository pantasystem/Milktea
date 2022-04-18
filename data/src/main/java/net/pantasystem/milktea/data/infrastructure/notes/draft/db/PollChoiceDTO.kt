package net.pantasystem.milktea.data.infrastructure.notes.draft.db

import androidx.room.*


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
    val choice: String,
    @ColumnInfo(name = "draft_note_id") val draftNoteId: Long,
    val weight: Int
)