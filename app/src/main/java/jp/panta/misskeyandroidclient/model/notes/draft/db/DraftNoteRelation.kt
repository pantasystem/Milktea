package jp.panta.misskeyandroidclient.model.notes.draft.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote

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
    fun toDraftNote(): DraftNote{
        return draftNoteDTO.toDraftNote(visibilityUserIds, draftFiles, pollChoices)
    }

}