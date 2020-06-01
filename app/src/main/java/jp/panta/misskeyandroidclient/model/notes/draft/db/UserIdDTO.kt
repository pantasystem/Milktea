package jp.panta.misskeyandroidclient.model.notes.draft.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "user_id", primaryKeys = ["userId", "draft_note_id"])
data class UserIdDTO(
    val userId: String,
    @ColumnInfo(name = "draft_note_id") val draftNoteId: Long
)