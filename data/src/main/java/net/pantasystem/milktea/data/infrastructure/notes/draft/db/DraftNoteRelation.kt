package net.pantasystem.milktea.data.infrastructure.notes.draft.db

import androidx.room.*
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.model.notes.draft.DraftNote

@Entity
class DraftNoteRelation {
    @Embedded
    lateinit var draftNoteDTO: DraftNoteDTO

    @Relation(
        parentColumn = "draft_note_id",
        entityColumn = "draft_note_id",
        entity = DraftFileDTO::class
    )
    var draftFiles: List<DraftFileDTO>? = null

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
    var draftNoteFiles: List<DraftFileJunctionRef>? = null

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
            parentColumn = "filePropertyId",
            entityColumn = "id"
        )
    )
    var driveFileRecords: List<DriveFileRecord>? = null

    @Ignore
    fun toDraftNote(accountId: Long): DraftNote {
        return draftNoteDTO.toDraftNote(accountId, visibilityUserIds, draftFiles, pollChoices)
    }

}