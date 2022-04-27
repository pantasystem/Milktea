package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.model.user.report.Report
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.ui.SafeUnbox
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.media.MediaViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.notes.NoteState
import net.pantasystem.milktea.api.misskey.notes.favorite.CreateFavorite
import net.pantasystem.milktea.api.misskey.notes.favorite.DeleteFavorite
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.toDraftNote
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.poll.Vote
import net.pantasystem.milktea.model.notes.reaction.CreateReaction
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import net.pantasystem.milktea.model.notes.reaction.ToggleReactionUseCase
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject


data class SelectedReaction(val noteId: Note.Id, val reaction: String)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val encryption: Encryption,
    private val translationStore: NoteTranslationStore,
    private val draftNoteDAO: DraftNoteDao,
    private val noteRepository: NoteRepository,
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    val accountStore: AccountStore,
) : ViewModel() {
    private val TAG = "NotesViewModel"

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

    fun setTargetToReNote(note: PlaneNoteViewData) {
        //reNoteTarget.postValue(note)
        Log.d("NotesViewModel", "登録しました: $note")
        reNoteTarget.event = note
    }

    fun setTargetToReply(note: PlaneNoteViewData) {
        replyTarget.event = note
    }

    fun setTargetToShare(note: PlaneNoteViewData) {
        shareTarget.event = note
        loadNoteState(note)
    }

    fun setTargetToUser(user: User) {
        targetUser.event = user
    }

    fun setTargetToNote() {
        showNoteEvent.event = shareTarget.event?.toShowNote?.note
    }

    fun setTargetToNote(note: PlaneNoteViewData) {
        showNoteEvent.event = note.toShowNote.note
    }

    fun setShowNote(note: Note) {
        showNoteEvent.event = note
    }

    fun setShowReactionHistoryDialog(noteId: Note.Id?, type: String?) {
        noteId?.let {
            showReactionHistoryEvent.event =
                ReactionHistoryRequest(noteId, type)
        }
    }

    fun showRenotes(noteId: Note.Id?) {
        showRenotesEvent.event = noteId
    }

    fun postRenote() {
        val renoteId = reNoteTarget.event?.toShowNote?.note?.id
            ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val author = accountRepository.get(renoteId.accountId)
                noteRepository.create(
                    CreateNote(
                        renoteId = renoteId,
                        text = null,
                        visibility = Visibility.Public(true),
                        author = author
                    )
                )
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

    fun putQuoteRenoteTarget() {
        quoteRenoteTarget.event = reNoteTarget.event
    }

    /**
     * イベントにリアクション送信ボタンを押したことを登録する
     */
    fun setTargetToReaction(planeNoteViewData: PlaneNoteViewData) {
        //Log.d("NotesViewModel", "getAccount()?: $getAccount()?")
        val myReaction = planeNoteViewData.myReaction.value
        if (myReaction != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    syncDeleteReaction(planeNoteViewData)
                } catch (e: Exception) {
                    Log.d(TAG, "error", e)
                }
            }
        } else {
            reactionTarget.event = planeNoteViewData
        }
    }


    fun postReaction(reaction: String) {
        val targetNote = reactionTarget.event
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
    fun postReaction(planeNoteViewData: PlaneNoteViewData, reaction: String) {

        val id = planeNoteViewData.toShowNote.note.id
        if (!Reaction(reaction).isLocal()) {
            showRemoteReactionEmojiSuggestionDialog.event =
                SelectedReaction(noteId = id, reaction = reaction)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {

            toggleReactionUseCase(id, reaction).onFailure {

            }.onSuccess {

            }

        }
    }

    /**
     * 同期リアクション削除
     * 既にリアクションが含まれている場合のみ実行される
     */
    private suspend fun syncDeleteReaction(planeNoteViewData: PlaneNoteViewData) {
        if (planeNoteViewData.myReaction.value.isNullOrBlank()) {
            return
        }
        noteRepository.unreaction(planeNoteViewData.toShowNote.note.id)
    }

    fun addFavorite(note: PlaneNoteViewData? = shareTarget.event) {
        note ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getMisskeyAPI()?.createFavorite(
                    CreateFavorite(
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

    fun deleteFavorite(note: PlaneNoteViewData? = shareTarget.event) {
        note ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val res = getMisskeyAPI()?.deleteFavorite(
                    DeleteFavorite(
                        i = getAccount()?.getI(encryption)!!,
                        noteId = note.toShowNote.note.id.noteId
                    )
                )
                requireNotNull(res)
                res
            }.getOrNull() != null
            withContext(Dispatchers.Main) {
                statusMessage.event = if (result) {
                    "お気に入りから削除しました"
                } else {
                    "お気に入りの削除に失敗しました"
                }
            }
        }


    }


    fun removeNote(noteId: Note.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                noteRepository.delete(noteId)
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    statusMessage.event = "削除に成功しました"
                }
            }
        }

    }

    fun removeAndEditNote(note: NoteRelation) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {

                val id = draftNoteDAO.fullInsert(note.toDraftNote())
                val dn = draftNoteDAO.getDraftNote(note.note.id.accountId, id)!!
                noteRepository.delete(note.note.id)
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

    fun unRenote(planeNoteViewData: PlaneNoteViewData) {
        if (planeNoteViewData.isRenotedByMe) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    noteRepository.delete(planeNoteViewData.note.note.id)
                }.onSuccess {
                    if (it) {
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

    private fun loadNoteState(planeNoteViewData: PlaneNoteViewData) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val response = getMisskeyAPI()?.noteState(
                    NoteRequest(
                        i = getAccount()?.getI(encryption)!!,
                        noteId = planeNoteViewData.toShowNote.note.id.noteId
                    )
                )
                    ?.throwIfHasError()
                val nowNoteId = shareTarget.event?.toShowNote?.note?.id?.noteId
                if (nowNoteId == planeNoteViewData.toShowNote.note.id.noteId) {
                    val state = response?.body()!!
                    Log.d(TAG, "state: $state")
                    shareNoteState.postValue(state)
                }
            }.onFailure { t ->
                Log.e(TAG, "note stateの取得に失敗しました", t)
            }
        }

    }


    fun vote(noteId: Note.Id?, poll: Poll?, choice: Poll.Choice?) {
        if (noteId == null || poll == null || choice == null) {
             return
        }
        if (SafeUnbox.unbox(poll.canVote)) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    getMisskeyAPI()?.vote(
                        Vote(
                            i = getAccount()?.getI(encryption)!!,
                            choice = choice.index,
                            noteId = noteId.noteId
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
            translationStore.translate(noteId)
        }
    }

    private suspend fun getMisskeyAPI(): MisskeyAPI? {
        return runCatching {
            val account = accountRepository.getCurrentAccount()
            misskeyAPIProvider.get(account.instanceDomain)
        }.getOrNull()
    }

    fun getAccount(): Account? {
        return accountStore.currentAccount
    }


}