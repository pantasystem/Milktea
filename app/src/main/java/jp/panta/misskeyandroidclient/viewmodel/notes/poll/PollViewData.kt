package jp.panta.misskeyandroidclient.viewmodel.notes.poll

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.view.SafeUnbox
import java.lang.IndexOutOfBoundsException

class PollViewData(private val poll: Poll, val noteId: String){
    inner class Choice(val choice: Poll.Choice, val number: Int){
        val isVoted = MutableLiveData<Boolean>(choice.isVoted)

        val voteCount = MutableLiveData<Int>(choice.votes)

        /*val percentage = MediatorLiveData<Float>().apply{
            addSource(totalVote){
                val votes = voteCount.value?: 0
                this.value = votes / it.toFloat() * 100
            }
        }*/
        val text = choice.text
    }
    val totalVoteCount = MutableLiveData<Int>(poll.choices.sumBy {
        it.votes
    })

    val isMultiple = poll.multiple

    val canVote = MutableLiveData<Boolean>(poll.multiple || poll.choices.all {
        !it.isVoted
    })


    val choices = poll.choices.mapIndexed { index, choice ->
        Choice(choice, index)
    }



    fun update(choice: Int, isMine: Boolean = false){
        try{
            val choiceItem = choices[choice]
            val exCount = SafeUnbox.unbox(choiceItem.voteCount.value)
            val exTotalCount = SafeUnbox.unbox(totalVoteCount.value)
            choiceItem.voteCount.postValue(exCount + 1)
            totalVoteCount.postValue(exTotalCount + 1)
            choiceItem.isVoted.postValue(isMine)
            canVote.postValue(poll.multiple && (canVote.value?: true))
        }catch(e: IndexOutOfBoundsException){
            Log.d("PollViewData", "voteのアップデートに失敗しました")
        }
    }


}