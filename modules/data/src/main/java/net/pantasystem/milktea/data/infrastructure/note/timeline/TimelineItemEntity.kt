package net.pantasystem.milktea.data.infrastructure.note.timeline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteEntity

@Entity(
    tableName = "timeline_item",
    indices = [
        Index(value = ["account_id", "page_id", "note_id"], unique = true),
        Index(value = ["note_id"]),
        Index(value = ["note_local_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_local_id"],
        )
    ],
)
data class TimelineItemEntity(
    @ColumnInfo(name = "account_id")
    val accountId: Long,

    @ColumnInfo(name = "page_id")
    val pageId: Long,

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "note_local_id")
    val noteLocalId: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
)