package jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.notes.draft.DraftPoll
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import java.util.*
import kotlin.collections.ArrayList

class PollEditor() {


    enum class DeadLineType{
        INDEFINITE_PERIOD,
        DATE_AND_TIME
    }
    val choices = MutableLiveData<List<PollChoice>>()
    val isMutable = MutableLiveData<Boolean>()

    val expiresAt = MutableLiveData<Date>()

    val deadLineType = MutableLiveData<DeadLineType>(DeadLineType.INDEFINITE_PERIOD)

    constructor(choices: List<String>?, multiple: Boolean?, expiresAt: Date?) : this(){
        this.choices.postValue(
            choices?.map{
                PollChoice(it)
            }
        )
        this.isMutable.postValue(multiple)
        this.expiresAt.postValue(expiresAt)
    }

    constructor(poll: Poll?) : this(
        poll?.choices?.map{
            it.text
        },
        poll?.multiple,
        poll?.expiresAt
    )

    constructor(draftPoll: DraftPoll?) : this(
        draftPoll?.choices,
        draftPoll?.multiple,
        draftPoll?.expiresAt?.let{
            Date(it)
        }

    )

    fun makeAndAddChoice(){
        val choices = this.choices.value
        if(choices.isNullOrEmpty()){
            this.choices.value = listOf(PollChoice())
        }else{
            this.choices.value = ArrayList<PollChoice>(choices).apply{
                add(PollChoice())
            }
        }
    }
    fun removeChoice(choice: PollChoice){
        val choices = this.choices.value
        if(!choices.isNullOrEmpty()){
            this.choices.value = ArrayList<PollChoice>(choices).apply{
                remove(choice)
            }
        }
    }
    fun buildCreatePoll(): CreatePoll?{
        val choices = this.choices.value?.map{
            it.text.value
        }?.filter{
            it != null && it.isNotBlank()
        }?.filterNotNull()

        val expiresAt = if(deadLineType.value == DeadLineType.DATE_AND_TIME){
            this.expiresAt.value
        }else{
            null
        }
        if(choices != null && choices.size >= 2){
            return CreatePoll(
                choices,
                isMutable.value?: false,
                expiresAt?.time
            )
        }
        return null
    }

    fun toDraftPoll(): DraftPoll?{
        val choices = this.choices.value
            ?: return null
        return DraftPoll(choices = choices.mapNotNull{
                it.text.value
            },
            multiple = isMutable.value == true,
            expiresAt = expiresAt.value?.time
        )
    }

}
