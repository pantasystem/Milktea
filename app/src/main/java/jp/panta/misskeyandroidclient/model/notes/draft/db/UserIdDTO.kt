package jp.panta.misskeyandroidclient.model.notes.draft.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "user_id", primaryKeys = ["userId", "draft_note_id"],
        foreignKeys = [
        androidx.room.ForeignKey(
            childColumns = ["draft_note_id"],
            parentColumns = ["draft_note_id"],
            entity = jp.panta.misskeyandroidclient.model.notes.draft.DraftNote::class,
            onDelete = androidx.room.ForeignKey.CASCADE,
            onUpdate = androidx.room.ForeignKey.CASCADE
        )]
)
data class UserIdDTO(
    val userId: String,
    @ColumnInfo(name = "draft_note_id") val draftNoteId: Long
)