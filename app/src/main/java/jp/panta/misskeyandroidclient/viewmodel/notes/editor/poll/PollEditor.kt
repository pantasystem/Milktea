package jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll

class PollEditor {
    val defaultChoices = listOf(
        PollChoice(),
        PollChoice()
    )
    val choices = MutableLiveData<List<PollChoice>>(defaultChoices)
    val isMutable = MutableLiveData<Boolean>()

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

        if(choices != null && choices.size >= 2){
            return CreatePoll(
                choices,
                isMutable.value?: false,
                null
            )
        }
        return null
    }
}