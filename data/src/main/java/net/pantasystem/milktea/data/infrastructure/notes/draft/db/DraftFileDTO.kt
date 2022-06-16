package net.pantasystem.milktea.data.infrastructure.notes.draft.db

import androidx.room.*
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.file.File

@Entity(
    tableName = "draft_file_v2_table",
    indices = [Index(
        "draftNoteId",
        "filePropertyId",
        "localFileId",
        unique = true
    ), Index("draftNoteId"), Index("localFileId"), Index("filePropertyId")],
)
data class DraftFileJunctionRef(
    val draftNoteId: Long,
    val filePropertyId: Long?,
    val localFileId: Long?,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L
)

data class DraftFileRelation(
    @Embedded val draftFileDTO: DraftFileDTO,

    @Relation(
        parentColumn = "localFileId",
        entityColumn = "localFileId"
    )
    val localFile: DraftLocalFile?,

    @Relation(
        parentColumn = "filePropertyId",
        entityColumn = "id"
    )
    val fileProperty: DriveFileRecord?,
)


@Entity(tableName = "draft_local_file_v2_table")
data class DraftLocalFile(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "is_sensitive") val isSensitive: Boolean?,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "thumbnailUrl") val thumbnailUrl: String?,
    @ColumnInfo(name = "folder_id") val folderId: String?,
    @PrimaryKey(autoGenerate = true) val localFileId: Long = 0L,
)

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

    companion object{
        fun make(file: File, draftNoteId: Long): DraftFileDTO{
            return DraftFileDTO(
                file.name,
                file.remoteFileId?.fileId,
                file.path,
                file.isSensitive,
                file.type,
                file.thumbnailUrl,
                draftNoteId,
                file.folderId
            ).apply{
                this.fileId = file.localFileId
            }
        }
    }



    @Ignore
    fun toFile(accountId: Long): File {
        return File(
            name,
            filePath ?: "",
            type,
            remoteFileId?.let {
                FileProperty.Id(
                    accountId,
                    it
                )
            },
            fileId,
            thumbnailUrl,
            isSensitive,
            folderId

        )
    }
}