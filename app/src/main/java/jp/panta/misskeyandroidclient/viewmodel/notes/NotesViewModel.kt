package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.notes.*
import jp.panta.misskeyandroidclient.api.notes.CreateNote
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.poll.Vote
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistory
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.reaction.ReactionSelection
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.view.SafeUnbox
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.media.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.media.MediaViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.poll.PollViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotesViewModel(
    val miCore: MiCore,
    private val reactionHistoryDao: ReactionHistoryDao
) : ViewModel(), ReactionSelection{
    private val TAG = "NotesViewModel"
    val encryption = miCore.getEncryption()

    val statusMessage = EventBus<String>()

    val errorStatusMessage = EventBus<String>()

    val reNoteTarget = EventBus<PlaneNoteViewData>()

    val quoteRenoteTarget = EventBus<PlaneNoteViewData>()

    val replyTarget = EventBus<PlaneNoteViewData>()

    val reactionTarget = EventBus<PlaneNoteViewData>()

    val submittedNotesOnReaction = EventBus<PlaneNoteViewData>(0)

    val shareTarget = EventBus<PlaneNoteViewData>()

    val confirmDeletionEvent = EventBus<PlaneNoteViewData>()

    val confirmDeleteAndEditEvent = EventBus<PlaneNoteViewData>()

    val shareNoteState = MutableLiveData<State>()

    val targetUser = EventBus<UserDTO>()

    val targetNote = EventBus<PlaneNoteViewData>()

    val showNoteEvent = EventBus<NoteDTO>()

    val targetFile = EventBus<Pair<FileViewData, MediaViewData>>()

    val showInputReactionEvent = EventBus<Unit>()

    val openNoteEditor = EventBus<NoteDTO?>()

    fun setTargetToReNote(note: PlaneNoteViewData){
        //reNoteTarget.postValue(note)
        Log.d("NotesViewModel", "登録しました: $note")
        reNoteTarget.event = note
    }

    fun setTargetToReply(note: PlaneNoteViewData){
        replyTarget.event = note
    }

    fun setTargetToShare(note: PlaneNoteViewData){
        shareTarget.event = note
        loadNoteState(note)
    }

    fun setTargetToUser(user: UserDTO){
        targetUser.event = user
    }

    fun setTargetToNote(){
        targetNote.event = shareTarget.event
    }
    fun setTargetToNote(note: PlaneNoteViewData){
        targetNote.event = note
    }

    fun setShowNote(note: NoteDTO){
        showNoteEvent.event = note
    }

    fun postRenote(){
        val renoteId = reNoteTarget.event?.toShowNote?.note?.id?.noteId
        if(renoteId != null){
            val request = CreateNote(i = getAccount()?.getI(encryption)!!, text = null, renoteId = renoteId)
            getMisskeyAPI()?.create(request)?.enqueue(object : Callback<CreateNote.Response>{
                override fun onResponse(
                    call: Call<CreateNote.Response>,
                    response: Response<CreateNote.Response>
                ) {
                    statusMessage.event = "renoteしました"

                }
                override fun onFailure(call: Call<CreateNote.Response>, t: Throwable) {
                    errorStatusMessage.event = "renote失敗しました"

                }

            })
        }
    }

    fun putQuoteRenoteTarget(){
        quoteRenoteTarget.event = reNoteTarget.event
    }

    /**
     * イベントにリアクション送信ボタンを押したことを登録する
     */
    fun setTargetToReaction(planeNoteViewData: PlaneNoteViewData){
        //Log.d("NotesViewModel", "getAccount()?: $getAccount()?")
        val myReaction = planeNoteViewData.myReaction.value
        if(myReaction != null){
            viewModelScope.launch(Dispatchers.IO){
                try{
                    syncDeleteReaction(planeNoteViewData)
                }catch(e: Exception){
                    Log.d(TAG, "error", e)
                }
            }
        }else{
            reactionTarget.event = planeNoteViewData
        }
    }

    override fun selectReaction(reaction: String) {
        postReaction(reaction)
    }

    //setTargetToReactionが呼び出されている必要がある
    fun postReaction(reaction: String){
        val targetNote =  reactionTarget.event
        if(targetNote != null){
            postReaction(targetNote, reaction)
        }
    }

    /**
     * リアクションを送信する
     * @param reaction 既存のリアクションと値が同様の場合は解除のみする
     * 既に含まれているmyReactionと一致しない場合は一度解除し再送する
     */
    fun postReaction(planeNoteViewData: PlaneNoteViewData, reaction: String){
        val myReaction = planeNoteViewData.myReaction.value

        viewModelScope.launch(Dispatchers.IO){
            //リアクション解除処理をする
            submittedNotesOnReaction.event = planeNoteViewData
            Log.d("NotesViewModel", "postReaction(n, n)")
            try{
                if(myReaction != null){
                    syncDeleteReaction(planeNoteViewData)
                }
                if(reaction == myReaction){
                    return@launch
                }
                val res = getMisskeyAPI()?.createReaction(
                    CreateReaction(
                    i = getAccount()?.getI(encryption)!!,
                    reaction = reaction,
                    noteId = planeNoteViewData.toShowNote.note.id.noteId
                )
                )?.execute()
                if(res?.code() in 200 until 300){
                    syncAddReactionHistory(reaction)
                }
                Log.d("NotesViewModel", "結果: $res")
            }catch(e: Exception){
                Log.e("NotesViewModel", "postReaction error", e)
            }

        }
    }

    /**
     * 同期リアクション削除
     * 既にリアクションが含まれている場合のみ実行される
     */
    private fun syncDeleteReaction(planeNoteViewData: PlaneNoteViewData){
        planeNoteViewData.myReaction.value?: return
        getMisskeyAPI()?.deleteReaction(
            DeleteNote(
            i = getAccount()?.getI(encryption)!!,
            noteId = planeNoteViewData.toShowNote.note.id.noteId
        )
        )?.execute()
    }

    private fun syncAddReactionHistory(reaction: String){
        try{
            val domain = getAccount()?.instanceDomain
            reactionHistoryDao.insert(ReactionHistory(instanceDomain = domain!!, reaction = reaction))
        }catch(e: Exception){
            Log.e(TAG, "reaction追加中にエラー発生", e)
        }
    }

    fun addFavorite(note: PlaneNoteViewData? = shareTarget.event){
        note?: return

        getMisskeyAPI()?.createFavorite(
            NoteRequest(
                i = getAccount()?.getI(encryption)!!,
                noteId = note.toShowNote.note.id.noteId
            )
        )?.enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Log.d(TAG, "お気に入りに追加しました")
                statusMessage.event = "お気に入りに追加しました"
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "お気に入りに追加失敗しました", t)
                statusMessage.event = "お気に入りにへの追加に失敗しました"
            }
        })

    }

    fun deleteFavorite(note: PlaneNoteViewData? = shareTarget.event){
        note?: return

        getMisskeyAPI()?.deleteFavorite(
            NoteRequest(
                i = getAccount()?.getI(encryption)!!,
                noteId = note.toShowNote.note.id.noteId
            )
        )?.enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Log.d(TAG, "お気に入りから削除しました")
                statusMessage.event = "お気に入りから削除しました"
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "お気に入りの削除に追加失敗しました", t)
                statusMessage.event = "お気に入りの削除に失敗しました"
            }
        })
    }


    fun removeNote(note: NoteDTO){
        getMisskeyAPI()?.delete(
            DeleteNote(
                i = getAccount()?.getI(encryption)!!,
                noteId = note.id
            )
        )?.enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Log.d(TAG, "削除に成功しました")
                if(response.code() == 204){
                    statusMessage.event = "削除に成功しました"
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(TAG, "削除に失敗しました")
            }
        })
    }

    fun removeAndEditNote(note: NoteDTO){
        getMisskeyAPI()?.delete(
            DeleteNote(
                i = getAccount()?.getI(encryption)!!,
                noteId = note.id
            )
        )?.enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    openNoteEditor.event = note
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "削除に失敗しました", t)
            }
        })
    }

    fun unRenote(planeNoteViewData: PlaneNoteViewData){
        if(planeNoteViewData.isRenotedByMe){
            getMisskeyAPI()?.delete(
                DeleteNote(i = getAccount()?.getI(miCore.getEncryption())!!, noteId = planeNoteViewData.note.note.id.noteId)
            )?.enqueue(object : Callback<Unit>{
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if(response.code() in 200 until 300){
                        statusMessage.event = "削除に成功しました"
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.d(TAG, "unrenote失敗")
                }
            })
        }

    }

    private fun loadNoteState(planeNoteViewData: PlaneNoteViewData){
        getMisskeyAPI()?.noteState(NoteRequest(i = getAccount()?.getI(encryption)!!, noteId = planeNoteViewData.toShowNote.note.id.noteId))
            ?.enqueue(object : Callback<State>{
                override fun onResponse(call: Call<State>, response: Response<State>) {
                    val nowNoteId = shareTarget.event?.toShowNote?.note?.id?.noteId
                    if(nowNoteId == planeNoteViewData.toShowNote.note.id.noteId){
                        val state = response.body()?: return
                        Log.d(TAG, "state: $state")
                        shareNoteState.postValue(state)
                    }
                }
                override fun onFailure(call: Call<State>, t: Throwable) {
                    Log.e(TAG, "note stateの取得に失敗しました", t)
                }
            })
    }

    fun showFile(media: MediaViewData, file: FileViewData){
        targetFile.event = Pair(file, media)
    }

    fun vote(poll: PollViewData, choice: PollViewData.Choice){
        if(SafeUnbox.unbox(poll.canVote.value)){
            getMisskeyAPI()?.vote(
                Vote(
                    i = getAccount()?.getI(encryption)!!,
                    choice = choice.number,
                    noteId = poll.noteId
                )
            )?.enqueue(object : Callback<Unit>{
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if(response.code() == 204){
                        Log.d(TAG, "投票に成功しました")
                    }else{
                        Log.d(TAG, "投票に失敗しました")
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.d(TAG, "投票に失敗しました")
                }
            })
        }
    }
    
    private fun getMisskeyAPI(): MisskeyAPI?{
        return miCore.getCurrentAccount().value?.let{
            miCore.getMisskeyAPI(it)
        }
    }

    fun getAccount(): Account?{
        return miCore.getCurrentAccount().value
    }

    
}