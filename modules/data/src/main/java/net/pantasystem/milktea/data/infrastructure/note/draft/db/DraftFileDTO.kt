package net.pantasystem.milktea.data.infrastructure.note.draft.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.model.note.draft.DraftNoteFile

@Entity(
    tableName = "draft_file_v2_table",
    indices = [Index(
        "draftNoteId",
        "filePropertyId",
        "localFileId",
        unique = true
    ), Index("draftNoteId"), Index("localFileId"), Index("filePropertyId")],
    foreignKeys = [
        ForeignKey(
            entity = DriveFileRecord::class,
            parentColumns = ["id"],
            childColumns = ["filePropertyId"],
            onDelete = OnConflictStrategy.ABORT,
            onUpdate = OnConflictStrategy.REPLACE,
        ),
        ForeignKey(
            entity = DraftLocalFile::class,
            parentColumns = ["localFileId"],
            childColumns = ["localFileId"],
            onDelete = OnConflictStrategy.ABORT,
            onUpdate = OnConflictStrategy.REPLACE,
        )

    ]
)
data class DraftFileJunctionRef(
    @ColumnInfo(name = "draftNoteId")
    val draftNoteId: Long,

    @ColumnInfo(name = "filePropertyId")
    val filePropertyId: Long?,

    @ColumnInfo(name = "localFileId")
    val localFileId: Long?,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0L
) {
    companion object
}




@Entity(tableName = "draft_local_file_v2_table")
data class DraftLocalFile(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "is_sensitive") val isSensitive: Boolean?,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "thumbnailUrl") val thumbnailUrl: String?,
    @ColumnInfo(name = "folder_id") val folderId: String?,
    @ColumnInfo(name = "file_size") val fileSize: Long?,
    @ColumnInfo(name = "comment") val comment: String?,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "localFileId")
    val localFileId: Long = 0L,
) {
    companion object
}

fun DraftLocalFile.Companion.from(draftNote: DraftNoteFile.Local): DraftLocalFile {
    return DraftLocalFile(
        localFileId = draftNote.localFileId,
        filePath = draftNote.filePath,
        folderId = draftNote.folderId,
        name = draftNote.name,
        type = draftNote.type,
        thumbnailUrl = draftNote.thumbnailUrl,
        isSensitive = draftNote.isSensitive,
        fileSize = draftNote.fileSize,
        comment = draftNote.comment
    )
}
