package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.CreateNote
import jp.panta.misskeyandroidclient.model.notes.CreateReaction
import jp.panta.misskeyandroidclient.model.notes.DeleteNote
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotesViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI
) : ViewModel(){

    val statusMessage = MutableLiveData<String>()

    val errorStatusMessage = MutableLiveData<String>()

    val reNoteTarget = MutableLiveData<PlaneNoteViewData>()

    val quoteRenoteTarget = MutableLiveData<PlaneNoteViewData>()

    val replyTarget = MutableLiveData<PlaneNoteViewData>()

    val reactionTarget = MutableLiveData<PlaneNoteViewData>()

    val submittedNotesOnReaction = MutableLiveData<PlaneNoteViewData>()

    val shareTarget = MutableLiveData<PlaneNoteViewData>()

    val targetUser = MutableLiveData<User>()


    fun setTargetToReNote(note: PlaneNoteViewData){
        reNoteTarget.postValue(note)
    }

    fun setTargetToReply(note: PlaneNoteViewData){
        replyTarget.postValue(note)
    }

    fun setTargetToShare(note: PlaneNoteViewData){
        shareTarget.postValue(note)
    }

    fun setTargetToUser(user: User){
        targetUser.postValue(user)
    }

    fun postRenote(){
        val renoteId = reNoteTarget.value?.toShowNote?.id
        if(renoteId != null){
            val request = CreateNote(i = connectionInstance.getI()!!, text = null, renoteId = renoteId)
            misskeyAPI.create(request).enqueue(object : Callback<Note?>{
                override fun onResponse(call: Call<Note?>, response: Response<Note?>) {
                    statusMessage.postValue("renoteしました")
                }

                override fun onFailure(call: Call<Note?>, t: Throwable) {
                    errorStatusMessage.postValue("renote失敗")
                }
            })
        }
    }

    fun putQuoteRenoteTarget(){
        quoteRenoteTarget.postValue(reNoteTarget.value)
    }

    //直接送信
    fun postReaction(planeNoteViewData: PlaneNoteViewData, reaction: String){
        val myReaction = planeNoteViewData.myReaction.value

        viewModelScope.launch(Dispatchers.IO){
            //リアクション解除処理をする
            try{
                if(myReaction != null){
                    misskeyAPI.deleteReaction(
                        DeleteNote(
                            i = connectionInstance.getI()!!,
                            noteId = planeNoteViewData.toShowNote.id
                        )
                    ).execute()

                }
                misskeyAPI.createReaction(CreateReaction(
                    i = connectionInstance.getI()!!,
                    reaction = reaction,
                    noteId = planeNoteViewData.toShowNote.id
                )).execute()
                submittedNotesOnReaction.postValue(planeNoteViewData)
            }catch(e: Exception){
                Log.e("NotesViewModel", "postReaction error", e)
            }

        }
    }

    fun setTargetToReaction(planeNoteViewData: PlaneNoteViewData){
        reactionTarget.postValue(planeNoteViewData)
    }

    //setTargetToReactionが呼び出されている必要がある
    fun postReaction(reaction: String){
        val targetNote =  reactionTarget.value
        if(targetNote != null){
            postReaction(targetNote, reaction)
        }
    }
}