package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.notes.NoteState
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.toDraftNote
import jp.panta.misskeyandroidclient.model.notes.poll.Vote
import jp.panta.misskeyandroidclient.model.notes.reaction.CreateReaction
import jp.panta.misskeyandroidclient.model.notes.reaction.Reaction
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryRequest
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistory
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.report.Report
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.ui.SafeUnbox
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.media.MediaViewData
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.poll.PollViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

//sealed interface Action {
//    data class ShowRenoteDialog(val note: PlaneNoteViewData) : Action
//    data class ShowQuoteEditor(val note: PlaneNoteViewData) : Action
//    data class ShowReplyEditor(val note: PlaneNoteViewData) : Action
//    data class ShowReactionPicker(val note: PlaneNoteViewData) : Action
//    data class ShowOptionalDialog(val note: PlaneNoteViewData) : Action
//    data class ShowReportConfirmDialog(val report: Report) : Action
//    data class ShowNoteDetail(val note: PlaneNoteViewData) : Action
//    data class ShowUserDetail(val user: User) : Action
//
//}
//
//sealed interface NotesState {
//
//    sealed interface SendRenoteState : NotesState {
//        val note: Note
//        data class Sending(override val note: Note) : SendRenoteState
//        data class Success(override val note: Note) : SendRenoteState
//        data class Failure(override val note: Note) : SendRenoteState
//    }
//
//    data class GetNoteStatusState (val note: Note, val noteState: State<NoteState>) : NotesState
//
//
//}
data class SelectedReaction(val noteId: Note.Id, val reaction: String)

@HiltViewModel
class NotesViewModel @Inject constructor(
    val miCore: MiCore,
    private val reactionHistoryDao: ReactionHistoryDao
) : ViewModel() {
    private val TAG = "NotesViewModel"
    val encryption = miCore.getEncryption()

    val statusMessage = EventBus<String>()

    private val errorStatusMessage = EventBus<String>()

    val reNoteTarget = EventBus<PlaneNoteViewData>()

    val quoteRenoteTarget = EventBus<PlaneNoteViewData>()

    val replyTarget = EventBus<PlaneNoteViewData>()

    val reactionTarget = EventBus<PlaneNoteViewData>()

    val shareTarget = EventBus<PlaneNoteViewData>()

    val confirmDeletionEvent = EventBus<PlaneNoteViewData>()

    val confirmDeleteAndEditEvent = EventBus<PlaneNoteViewData>()

    val confirmReportEvent = EventBus<Report>()

    val shareNoteState = MutableLiveData<NoteState>()

    val targetUser = EventBus<User>()

    val showNoteEvent = EventBus<Note>()

    val targetFile = EventBus<Pair<FileViewData, MediaViewData>>()

    val showInputReactionEvent = EventBus<Unit>()

    val openNoteEditor = EventBus<DraftNote?>()

    val showReactionHistoryEvent = EventBus<ReactionHistoryRequest?>()

    val showRenotesEvent = EventBus<Note.Id?>()

    /**
     * リモートのリアクションを選択したときに
     * ローカルの絵文字からそれに近い候補を表示するためのダイアログを表示するイベント
     */
    val showRemoteReactionEmojiSuggestionDialog = EventBus<SelectedReaction?>()

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

    fun setTargetToUser(user: User){
        targetUser.event = user
    }

    fun setTargetToNote(){
        showNoteEvent.event = shareTarget.event?.toShowNote?.note
    }

    fun setTargetToNote(note: PlaneNoteViewData){
        showNoteEvent.event = note.toShowNote.note
    }

    fun setShowNote(note: Note){
        showNoteEvent.event = note
    }

    fun setShowReactionHistoryDialog(noteId: Note.Id?, type: String?) {
        noteId?.let {
            showReactionHistoryEvent.event = ReactionHistoryRequest(noteId, type)
        }
    }

    fun showRenotes(noteId: Note.Id?) {
        showRenotesEvent.event = noteId
    }

    fun postRenote(){
        val renoteId = reNoteTarget.event?.toShowNote?.note?.id
            ?:return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val author = miCore.getAccountRepository().get(renoteId.accountId)
                miCore.getNoteRepository().create(CreateNote(renoteId = renoteId, text = null, visibility = Visibility.Public(true), author = author))
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    statusMessage.event = "renoteしました"
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    errorStatusMessage.event = "renote失敗しました"
                }
            }

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


    fun postReaction(reaction: String){
        val targetNote =  reactionTarget.event
        require(targetNote != null) {
            "targetNoteはNotNullである必要があります。"
        }
        postReaction(targetNote, reaction)
    }

    /**
     * リアクションを送信する
     * @param reaction 既存のリアクションと値が同様の場合は解除のみする
     * 既に含まれているmyReactionと一致しない場合は一度解除し再送する
     */
    fun postReaction(planeNoteViewData: PlaneNoteViewData, reaction: String){

        val id = planeNoteViewData.toShowNote.note.id
        if(!Reaction(reaction).isLocal()) {
            showRemoteReactionEmojiSuggestionDialog.event = SelectedReaction(noteId = id, reaction = reaction)
            return
        }
        viewModelScope.launch(Dispatchers.IO){

            runCatching {
                val result = miCore.getNoteRepository().toggleReaction(
                    CreateReaction(
                        noteId = id,
                        reaction = reaction
                    )
                )
                if(result) {
                    syncAddReactionHistory(reaction)
                }
            }


        }
    }

    /**
     * 同期リアクション削除
     * 既にリアクションが含まれている場合のみ実行される
     */
    private suspend fun syncDeleteReaction(planeNoteViewData: PlaneNoteViewData){
        if(planeNoteViewData.myReaction.value.isNullOrBlank()) {
            return
        }
        miCore.getNoteRepository().unreaction(planeNoteViewData.toShowNote.note.id)
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
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getMisskeyAPI()?.createFavorite(
                    NoteRequest(
                        i = getAccount()?.getI(encryption)!!,
                        noteId = note.toShowNote.note.id.noteId
                    )
                )
            }.onSuccess {
                requireNotNull(it)
                Log.d(TAG, "お気に入りに追加しました")
                withContext(Dispatchers.Main) {
                    statusMessage.event = "お気に入りに追加しました"
                }
            }.onFailure { t ->
                Log.e(TAG, "お気に入りに追加失敗しました", t)
                withContext(Dispatchers.Main) {
                    statusMessage.event = "お気に入りにへの追加に失敗しました"
                }
            }
        }
    }

    fun deleteFavorite(note: PlaneNoteViewData? = shareTarget.event){
        note?: return
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val res = getMisskeyAPI()?.deleteFavorite(
                    NoteRequest(
                        i = getAccount()?.getI(encryption)!!,
                        noteId = note.toShowNote.note.id.noteId
                    )
                )
                requireNotNull(res)
                res
            }.getOrNull() != null
            withContext(Dispatchers.Main) {
                statusMessage.event = if(result) {
                    "お気に入りから削除しました"
                }else{
                    "お気に入りの削除に失敗しました"
                }
            }
        }


    }


    fun removeNote(noteId: Note.Id){
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                miCore.getNoteRepository().delete(noteId)
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    statusMessage.event = "削除に成功しました"
                }
            }
        }

    }

    fun removeAndEditNote(note: NoteRelation){
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {

                val id = miCore.getDraftNoteDAO().fullInsert(note.toDraftNote())
                val dn = miCore.getDraftNoteDAO().getDraftNote(note.note.id.accountId, id)!!
                miCore.getNoteRepository().delete(note.note.id)
                dn
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    openNoteEditor.event = it
                }
            }.onFailure { t ->
                Log.e(TAG, "削除に失敗しました", t)

            }
        }

    }

    fun unRenote(planeNoteViewData: PlaneNoteViewData){
        if(planeNoteViewData.isRenotedByMe){
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    miCore.getNoteRepository().delete(planeNoteViewData.note.note.id)
                }.onSuccess {
                    if(it) {
                        withContext(Dispatchers.Main) {
                            statusMessage.event = "削除に成功しました"
                        }
                    }
                }.onFailure { t ->
                    Log.d(TAG, "unrenote失敗", t)
                }
            }

        }

    }

    private fun loadNoteState(planeNoteViewData: PlaneNoteViewData){
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val response = getMisskeyAPI()?.noteState(NoteRequest(i = getAccount()?.getI(encryption)!!, noteId = planeNoteViewData.toShowNote.note.id.noteId))
                    ?.throwIfHasError()
                val nowNoteId = shareTarget.event?.toShowNote?.note?.id?.noteId
                if(nowNoteId == planeNoteViewData.toShowNote.note.id.noteId){
                    val state = response?.body()!!
                    Log.d(TAG, "state: $state")
                    shareNoteState.postValue(state)
                }
            }.onFailure { t->
                Log.e(TAG, "note stateの取得に失敗しました", t)
            }
        }

    }


    fun vote(poll: PollViewData, choice: PollViewData.Choice){
        if(SafeUnbox.unbox(poll.canVote.value)){
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    getMisskeyAPI()?.vote(
                        Vote(
                            i = getAccount()?.getI(encryption)!!,
                            choice = choice.number,
                            noteId = poll.noteId
                        )
                    )?.throwIfHasError()
                }.onSuccess {
                    Log.d(TAG, "投票に成功しました")
                }.onFailure {
                    Log.d(TAG, "投票に失敗しました")
                }
            }
        }
    }

    fun translate(noteId: Note.Id) {
        viewModelScope.launch(Dispatchers.IO) {

            miCore.getTranslationStore().translate(noteId)
        }
    }
    
    private fun getMisskeyAPI(): MisskeyAPI?{
        return miCore.getCurrentAccount().value?.let{
            miCore.getMisskeyAPIProvider().get(it)
        }
    }

    fun getAccount(): Account?{
        return miCore.getCurrentAccount().value
    }

    
}