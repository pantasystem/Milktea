package jp.panta.misskeyandroidclient.model.notes.draft.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import jp.panta.misskeyandroidclient.model.notes.draft.DraftFile
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote

@Entity(
    tableName = "draft_file",
    foreignKeys = [
        ForeignKey(
            childColumns = ["draft_note_id"],
            parentColumns = ["draft_note_id"],
            entity = DraftNote::class,
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ])
data class DraftFileDTO(
    @ColumnInfo(name = "remote_file_id") val remoteFileId: String?,
    @ColumnInfo(name = "file_path") val filePath: String?,
    @ColumnInfo(name = "draft_note_id") val draftNoteId: Long
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "file_id")
    var fileId: Int? = null

    companion object{
        fun make(draftFile: DraftFile): DraftFileDTO{
            return DraftFileDTO(
                draftFile.remoteFileId,
                draftFile.filePath,
                draftFile.draftNoteId
            ).apply{
                this.fileId = draftFile.fileId
            }
        }
    }

    fun toDraftFile(): DraftFile{
        return DraftFile(
            remoteFileId,
            filePath,
            draftNoteId
        ).apply{
            this.fileId = this@DraftFileDTO.fileId
        }
    }
}