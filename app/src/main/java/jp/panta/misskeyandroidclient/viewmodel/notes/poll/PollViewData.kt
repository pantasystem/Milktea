package jp.panta.misskeyandroidclient.viewmodel.notes.poll

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.view.SafeUnbox
import java.lang.IndexOutOfBoundsException

class PollViewData(poll: Poll, val noteId: String){
    inner class Choice(val choice: Poll.Choice, totalVote: MediatorLiveData<Int>, val number: Int, canVote: MediatorLiveData<Boolean>, isMultiple: Boolean){
        val isVoted = MutableLiveData<Boolean>(choice.isVoted).apply{
            canVote.addSource(this){
                canVote.value = !(it && !isMultiple)
            }
        }

        val voteCount = MutableLiveData<Int>(choice.votes).apply{
            totalVote.addSource(this){
                val exTotal = totalVote.value?: 0
                totalVote.value = exTotal + it
            }
        }

        val percentage = MediatorLiveData<Float>().apply{
            addSource(totalVote){
                val votes = voteCount.value?: 0
                this.value = votes / it.toFloat() * 100
            }
        }
        val text = choice.text
    }
    val totalVoteCount = MediatorLiveData<Int>()

    val isMultiple = poll.multiple

    val canVote = MediatorLiveData<Boolean>()


    val choices = poll.choices.mapIndexed { index, choice ->
        Choice(choice, totalVoteCount, index, canVote, isMultiple)
    }

    val isVotingMode = MutableLiveData<Boolean>()


    fun update(choice: Int, isMine: Boolean = false){
        try{
            val choiceItem = choices[choice]
            val exCount = SafeUnbox.unbox(choiceItem.voteCount.value)
            choiceItem.voteCount.postValue(exCount + 1)
            choiceItem.isVoted.postValue(isMine)
        }catch(e: IndexOutOfBoundsException){
            Log.d("PollViewData", "voteのアップデートに失敗しました")
        }
    }

}