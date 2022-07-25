package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.ui.SafeUnbox
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.notes.NoteState
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.NoteTranslationStore
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.draft.toDraftNote
import net.pantasystem.milktea.model.notes.favorite.FavoriteRepository
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.ToggleReactionUseCase
import net.pantasystem.milktea.model.notes.renote.CreateRenoteUseCase
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.report.Report
import javax.inject.Inject


@HiltViewModel
class NotesViewModel @Inject constructor(
    private val encryption: Encryption,
    private val translationStore: NoteTranslationStore,
    private val noteRepository: NoteRepository,
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    private val favoriteRepository: FavoriteRepository,
    private val renoteUseCase: CreateRenoteUseCase,
    val accountStore: AccountStore,
    val draftNoteRepository: DraftNoteRepository,
) : ViewModel() {
    private val TAG = "NotesViewModel"

    val statusMessage = EventBus<String>()

    private val errorStatusMessage = EventBus<String>()

    val quoteRenoteTarget = EventBus<Note>()

    val shareTarget = EventBus<PlaneNoteViewData>()

    val confirmDeletionEvent = EventBus<PlaneNoteViewData>()

    val confirmDeleteAndEditEvent = EventBus<PlaneNoteViewData>()

    val confirmReportEvent = EventBus<Report>()

    val shareNoteState = MutableLiveData<NoteState>()

    val targetUser = EventBus<User>()

    val openNoteEditor = EventBus<DraftNote?>()


    fun setTargetToShare(note: PlaneNoteViewData) {
        shareTarget.event = note
        loadNoteState(note)
    }

    fun setTargetToUser(user: User) {
        targetUser.event = user
    }


    fun renote(noteId: Note.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            renoteUseCase(noteId).onSuccess {
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
//
//    fun putQuoteRenoteTarget() {
//        quoteRenoteTarget.event = reNoteTarget.event
//    }

    fun showQuoteNoteEditor(noteId: Note.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            recursiveSearchHasContentNote(noteId).onSuccess { note ->
                withContext(Dispatchers.Main) {
                    quoteRenoteTarget.event = note
                }
            }

        }
    }

    private suspend fun recursiveSearchHasContentNote(noteId: Note.Id): Result<Note> = runCatching {
        val note = noteRepository.find(noteId).getOrThrow()
        if (note.hasContent()) {
            note
        } else {
            recursiveSearchHasContentNote(note.renoteId!!).getOrThrow()
        }
    }

    /**
     * リアクションを送信する
     * @param reaction 既存のリアクションと値が同様の場合は解除のみする
     * 既に含まれているmyReactionと一致しない場合は一度解除し再送する
     */
    fun postReaction(planeNoteViewData: PlaneNoteViewData, reaction: String) {

        val id = planeNoteViewData.toShowNote.note.id
        toggleReaction(id, reaction)
    }

    fun toggleReaction(noteId: Note.Id, reaction: String) {
        viewModelScope.launch(Dispatchers.IO) {
            toggleReactionUseCase(noteId, reaction).onFailure {
            }.onSuccess {

            }

        }
    }


    fun addFavorite(note: PlaneNoteViewData? = shareTarget.event) {
        note ?: return
        viewModelScope.launch(Dispatchers.IO) {
            favoriteRepository.create(note.toShowNote.note.id).onSuccess {
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
            val result = favoriteRepository.delete(note.toShowNote.note.id).getOrNull() != null
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
            noteRepository.delete(noteId).onSuccess {
                withContext(Dispatchers.Main) {
                    statusMessage.event = "削除に成功しました"
                }
            }
        }

    }

    fun removeAndEditNote(note: NoteRelation) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {

                val dn = draftNoteRepository.save(note.toDraftNote())
                    .getOrThrow()
                noteRepository.delete(note.note.id).getOrThrow()
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


    fun unRenote(noteId: Note.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.delete(noteId).onSuccess {
                withContext(Dispatchers.Main) {
                    statusMessage.event = "削除に成功しました"
                }
            }.onFailure { t ->
                Log.d(TAG, "unrenote失敗", t)
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
                )?.throwIfHasError()

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
                noteRepository.vote(noteId, choice).onSuccess {
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
            val account = accountRepository.getCurrentAccount().getOrThrow()
            misskeyAPIProvider.get(account.instanceDomain)
        }.getOrNull()
    }

    fun getAccount(): Account? {
        return accountStore.currentAccount
    }


}