package jp.panta.misskeyandroidclient.model.notes.draft.db

import jp.panta.misskeyandroidclient.model.notes.draft.DraftPoll

class DraftPollDTO(
    val multiple: Boolean,
    val expiresAt: Long? = null
){

    companion object{
       fun make(draftPoll: DraftPoll?): DraftPollDTO?{
           return draftPoll?.let{
               DraftPollDTO(draftPoll.multiple, draftPoll.expiresAt)
           }
       }
    }

    fun toDraftPoll(choices: List<PollChoiceDTO>): DraftPoll{
        return DraftPoll(
            choices.map{
                it.choice
            },
            multiple,
            expiresAt
        )
    }
}