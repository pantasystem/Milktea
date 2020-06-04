package jp.panta.misskeyandroidclient.model.notes.draft.db

import androidx.room.*
import jp.panta.misskeyandroidclient.model.file.File

@Entity(
    tableName = "draft_file",
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
                file.remoteFileId,
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
    fun toFile(): File{
        return File(
            name,
            filePath?: "",
            type,
            remoteFileId,
            fileId,
            thumbnailUrl,
            isSensitive,
            folderId

        )
    }
}