package net.pantasystem.milktea.data.infrastructure.note.draft.db

import androidx.room.*
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.draft.DraftNoteFile

@Entity
class DraftNoteRelation {
    @Embedded
    lateinit var draftNoteDTO: DraftNoteDTO

//    @Relation(
//        parentColumn = "draft_note_id",
//        entityColumn = "draft_note_id",
//        entity = DraftFileDTO::class
//    )
//    var draftFiles: List<DraftFileDTO>? = null

    @Relation(
        parentColumn = "draft_note_id",
        entityColumn = "draft_note_id",
        entity = UserIdDTO::class
    )
    var visibilityUserIds: List<UserIdDTO>? = null

    @Relation(
        parentColumn = "draft_note_id",
        entityColumn = "draft_note_id",
        entity = PollChoiceDTO::class
    )
    var pollChoices: List<PollChoiceDTO>? = null

    @Relation(parentColumn = "draft_note_id", entityColumn = "draftNoteId")
    var draftNoteJunctionRefs: List<DraftFileJunctionRef>? = null

    @Relation(
        parentColumn = "draft_note_id",
        entityColumn = "localFileId",
        associateBy = Junction(
            DraftFileJunctionRef::class,
            parentColumn = "draftNoteId"
        )
    )
    var localFiles: List<DraftLocalFile>? = null

    @Relation(
        parentColumn = "draft_note_id",
        entityColumn = "id",
        associateBy = Junction(
            DraftFileJunctionRef::class,
            parentColumn = "draftNoteId",
            entityColumn = "filePropertyId"
        )
    )
    var driveFileRecords: List<DriveFileRecord>? = null

    @Ignore
    fun getDraftNoteFiles(): List<DraftNoteFile> {
        return draftNoteJunctionRefs?.filter {
            it.filePropertyId != null || it.localFileId != null
        }?.mapNotNull { ref ->
            if (ref.localFileId != null) {
                localFiles?.firstOrNull {
                    it.localFileId == ref.localFileId
                }?.apply {
                    return@mapNotNull DraftNoteFile.Local(
                        name = name,
                        folderId = folderId,
                        filePath = filePath,
                        isSensitive = isSensitive,
                        thumbnailUrl = thumbnailUrl,
                        type = type,
                        localFileId = ref.localFileId,
                        fileSize = fileSize,
                        comment = comment
                    )
                }

            }
            if (ref.filePropertyId != null) {
                driveFileRecords?.firstOrNull {
                    it.id == ref.filePropertyId
                }?.apply {
                    return@mapNotNull DraftNoteFile.Remote(toFileProperty())
                }
            }
            null
        }?: emptyList()
    }


    @Ignore
    fun toDraftNote(accountId: Long): DraftNote {

        return draftNoteDTO.toDraftNote(accountId, visibilityUserIds, pollChoices, getDraftNoteFiles())
    }
}