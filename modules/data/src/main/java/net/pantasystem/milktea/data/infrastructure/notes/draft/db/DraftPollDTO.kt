package net.pantasystem.milktea.data.infrastructure.notes.draft.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import net.pantasystem.milktea.model.note.draft.DraftPoll

@Entity
class DraftPollDTO(
    @ColumnInfo(name = "multiple")
    val multiple: Boolean,

    @ColumnInfo(name = "expiresAt")
    val expiresAt: Long? = null
){

    companion object{

        @JvmStatic
        fun make(draftPoll: DraftPoll?): DraftPollDTO?{
            return draftPoll?.let{
               DraftPollDTO(draftPoll.multiple, draftPoll.expiresAt)
           }
        }
    }

    @Ignore
    fun toDraftPoll(choices: List<PollChoiceDTO>?): DraftPoll?{
        if(choices.isNullOrEmpty()){
            return null
        }
        return DraftPoll(
            choices.sortedBy {
                it.weight
            }.map{
                it.choice
            },
            multiple,
            expiresAt
        )
    }
}