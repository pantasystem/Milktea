package net.pantasystem.milktea.data.model.notes.draft.db

import androidx.room.*
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.file.File

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
        fun make(file: net.pantasystem.milktea.model.file.File, draftNoteId: Long): DraftFileDTO{
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
    fun toFile(accountId: Long): net.pantasystem.milktea.model.file.File {
        return net.pantasystem.milktea.model.file.File(
            name,
            filePath ?: "",
            type,
            remoteFileId?.let {
                net.pantasystem.milktea.model.drive.FileProperty.Id(
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