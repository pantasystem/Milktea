package net.pantasystem.milktea.data.infrastructure.notes.draft.db

import androidx.room.*
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.model.notes.draft.DraftNoteFile

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
    val draftNoteId: Long,
    val filePropertyId: Long?,
    val localFileId: Long?,
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
    @PrimaryKey(autoGenerate = true) val localFileId: Long = 0L,
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

@Suppress("DEPRECATION")
@Entity(
    tableName = "draft_file_table",
    foreignKeys = [
        ForeignKey(
            childColumns = ["draft_note_id"],
            parentColumns = ["draft_note_id"],
            entity = DraftNoteDTO::class,
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("draft_note_id")]
)
@Deprecated("DraftFileDTOV2へ移行")
data class DraftFileDTO(
    @ColumnInfo(defaultValue = "name none") val name: String,
    @ColumnInfo(name = "remote_file_id") val remoteFileId: String?,
    @ColumnInfo(name = "file_path") val filePath: String?,
    @ColumnInfo(name = "is_sensitive") val isSensitive: Boolean?,
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name ="thumbnailUrl") val thumbnailUrl: String?,
    @ColumnInfo(name = "draft_note_id") val draftNoteId: Long,
    @ColumnInfo(name = "folder_id") val folderId: String?
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "file_id")
    var fileId: Long? = null

}