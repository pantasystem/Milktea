package net.pantasystem.milktea.note.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_android.ui.SafeUnbox
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.draft.toDraftNote
import net.pantasystem.milktea.model.notes.favorite.FavoriteRepository
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.ToggleReactionUseCase
import net.pantasystem.milktea.model.user.report.Report
import javax.inject.Inject


@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    private val favoriteRepository: FavoriteRepository,
    val accountStore: AccountStore,
    val draftNoteRepository: DraftNoteRepository,
) : ViewModel() {
    private val TAG = "NotesViewModel"

    val statusMessage = EventBus<String>()


    val quoteRenoteTarget = EventBus<Note>()

    val shareTarget = EventBus<PlaneNoteViewData>()

    val confirmDeletionEvent = EventBus<PlaneNoteViewData>()

    val confirmDeleteAndEditEvent = EventBus<NoteRelation>()

    val confirmReportEvent = EventBus<Report>()

    val openNoteEditor = EventBus<DraftNote?>()



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

            }
        }
    }


    fun addFavorite(noteId: Note.Id) {
        viewModelScope.launch {
            favoriteRepository.create(noteId).onSuccess {
                statusMessage.event = "お気に入りに追加しました"

            }.onFailure { t ->
                statusMessage.event = "お気に入りにへの追加に失敗しました"
            }
        }
    }

    fun deleteFavorite(noteId: Note.Id) {
        viewModelScope.launch {
            val result = favoriteRepository.delete(noteId).getOrNull() != null
            statusMessage.event = if (result) {
                "お気に入りから削除しました"
            } else {
                "お気に入りの削除に失敗しました"
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



}