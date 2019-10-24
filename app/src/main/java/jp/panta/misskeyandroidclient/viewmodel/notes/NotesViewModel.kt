package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.CreateNote
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.users.User
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
}