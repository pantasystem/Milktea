package net.pantasystem.milktea.data.model.notes.draft.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import net.pantasystem.milktea.data.model.notes.draft.DraftNote

@Entity
class DraftNoteRelation{
    @Embedded
    lateinit var draftNoteDTO: DraftNoteDTO

    @Relation(parentColumn = "draft_note_id", entityColumn = "draft_note_id", entity = DraftFileDTO::class)
    var draftFiles: List<DraftFileDTO>? = null

    @Relation(parentColumn = "draft_note_id", entityColumn = "draft_note_id", entity = UserIdDTO::class)
    var visibilityUserIds: List<UserIdDTO>? = null

    @Relation(parentColumn = "draft_note_id", entityColumn = "draft_note_id", entity = PollChoiceDTO::class)
    var pollChoices: List<PollChoiceDTO>? = null

    @Ignore
    fun toDraftNote(accountId: Long): DraftNote{
        return draftNoteDTO.toDraftNote(accountId, visibilityUserIds, draftFiles, pollChoices)
    }

}