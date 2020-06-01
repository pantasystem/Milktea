package jp.panta.misskeyandroidclient.model.notes.draft.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote

@Entity(
    tableName = "poll_choice",
    primaryKeys = ["choice", "draft_note_id", "weight"],
    foreignKeys = [
        ForeignKey(
            childColumns = ["draft_note_id"],
            parentColumns = ["draft_note_id"],
            entity = DraftNote::class,
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
data class PollChoiceDTO(
    val choice: String,
    @ColumnInfo(name = "draft_note_id") val draftNoteId: Long,
    val weight: Int
)