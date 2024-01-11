package net.pantasystem.milktea.note.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.handler.AppGlobalError
import net.pantasystem.milktea.app_store.handler.UserActionAppGlobalErrorAction
import net.pantasystem.milktea.app_store.handler.UserActionAppGlobalErrorStore
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.note.DeleteAndEditUseCase
import net.pantasystem.milktea.model.note.DeleteNoteUseCase
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.note.RecursiveSearchHasContentNoteUseCase
import net.pantasystem.milktea.model.note.bookmark.CreateBookmarkUseCase
import net.pantasystem.milktea.model.note.bookmark.DeleteBookmarkUseCase
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.favorite.CreateFavoriteUseCase
import net.pantasystem.milktea.model.note.favorite.DeleteFavoriteUseCase
import net.pantasystem.milktea.model.note.favorite.ToggleFavoriteUseCase
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.note.poll.VoteUseCase
import net.pantasystem.milktea.model.note.reaction.DeleteReactionsUseCase
import net.pantasystem.milktea.model.note.reaction.ToggleReactionUseCase
import net.pantasystem.milktea.model.note.repost.QuoteRenoteData
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.report.Report
import net.pantasystem.milktea.note.R
import javax.inject.Inject


@HiltViewModel
class NotesViewModel @Inject constructor(
    private val recursiveSearchHasContentNoteUseCase: RecursiveSearchHasContentNoteUseCase,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    private val createFavoriteUseCase: CreateFavoriteUseCase,
    private val deleteFavoriteUseCase: DeleteFavoriteUseCase,
    private val translationStore: NoteTranslationStore,
    private val createBookmarkUseCase: CreateBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val deleteAndEditUseCase: DeleteAndEditUseCase,
    private val deleteReactionUseCase: DeleteReactionsUseCase,
    private val voteUseCase: VoteUseCase,
    private val configRepository: LocalConfigRepository,
    private val userActionAppGlobalErrorStore: UserActionAppGlobalErrorStore,
    loggerFactory: Logger.Factory
) : ViewModel() {
    private val logger by lazy {
        loggerFactory.create("NotesViewModel")
    }

    private val _statusMessage = MutableSharedFlow<StringSource>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 10)
    val statusMessage = _statusMessage.asSharedFlow()

    val quoteRenoteTarget = MutableSharedFlow<QuoteRenoteData>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 10)

    val confirmDeletionEvent = MutableSharedFlow<NoteRelation?>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 10)

    val confirmDeleteAndEditEvent = MutableSharedFlow<NoteRelation?>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 10)

    val confirmReportEvent = MutableSharedFlow<Report?>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 10)

    private val _openNoteEditorEvent = MutableSharedFlow<DraftNote?>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 10)
    val openNoteEditorEvent = _openNoteEditorEvent.asSharedFlow()

    fun showQuoteNoteEditor(noteId: Note.Id) {
        viewModelScope.launch {
            // 引用リノートしようとしたノートが第三者によりリノートされたものである可能性もあるので、
            // リノートIDを辿って大本のノートを探し出す
            recursiveSearchHasContentNote(noteId).onSuccess {
                quoteRenoteTarget.tryEmit(
                    QuoteRenoteData.ofTimeline(it.id)
                )
            }.onFailure {
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.showQuoteNoteEditor",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Load note failed"),
                        throwable = it
                    )
                )
            }
        }
    }

    fun showQuoteToChannelNoteEditor(noteId: Note.Id) {
        viewModelScope.launch {
            recursiveSearchHasContentNote(noteId).onSuccess {
                quoteRenoteTarget.tryEmit(
                    QuoteRenoteData.ofChannel(it.id, requireNotNull(it.channelId))
                )
            }.onFailure {
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.showQuoteToChannelNoteEditor",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Load note failed"),
                        throwable = it
                    )
                )
            }
        }
    }

    private suspend fun recursiveSearchHasContentNote(noteId: Note.Id): Result<Note> =
        recursiveSearchHasContentNoteUseCase(noteId)

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
        viewModelScope.launch {
            toggleReactionUseCase(noteId, reaction).onFailure {
                logger.error("リアクション失敗", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.toggleReaction",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Reaction failed"),
                        throwable = it
                    )
                )
            }
        }
    }

    fun deleteReactions(noteId: Note.Id) {
        viewModelScope.launch {
            deleteReactionUseCase(noteId).onFailure {
                logger.error("リアクションの解除に失敗", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.deleteReactions",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Delete reactions failed"),
                        throwable = it
                    )
                )
            }
        }
    }

    fun addFavorite(noteId: Note.Id) {
        viewModelScope.launch {
            createFavoriteUseCase(noteId).onSuccess {
                _statusMessage.tryEmit(StringSource(R.string.successfully_added_to_favorites))
            }.onFailure {
                _statusMessage.tryEmit(StringSource(R.string.failed_to_add_to_favorites))
            }
        }
    }

    fun deleteFavorite(noteId: Note.Id) {
        viewModelScope.launch {
            deleteFavoriteUseCase(noteId).onFailure {
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.deleteFavorite",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource(R.string.failed_to_delete_favorites),
                        throwable = it
                    )
                )
            }.onSuccess {
                _statusMessage.tryEmit(StringSource(R.string.removed_from_favorites))
            }
        }
    }


    fun addBookmark(noteId: Note.Id) {
        viewModelScope.launch {
            createBookmarkUseCase(noteId).onFailure {
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.addBookmark",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Add bookmark failed"),
                        throwable = it
                    )
                )
            }
        }
    }

    fun removeBookmark(noteId: Note.Id) {
        viewModelScope.launch {
            deleteBookmarkUseCase(noteId).onFailure {
                logger.error("remove book mark error", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.removeBookmark",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Remove bookmark failed"),
                        throwable = it
                    )
                )
            }
        }
    }

    fun removeNote(noteId: Note.Id) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId).onSuccess {
                _statusMessage.tryEmit(StringSource(R.string.successfully_deleted))
            }.onFailure {
                logger.error("ノート削除に失敗", it)
                if (userActionAppGlobalErrorStore.dispatchAndAwaitUserAction(
                        AppGlobalError(
                            tag = "NotesViewModel.removeNote",
                            level = AppGlobalError.ErrorLevel.Error,
                            message = StringSource("Delete note failed"),
                            throwable = it,
                            retryable = true
                        ),
                        UserActionAppGlobalErrorAction.Type.Retry
                    )
                ) {
                    removeNote(
                        noteId
                    )
                }
            }
        }

    }

    fun removeAndEditNote(noteId: Note.Id) {
        viewModelScope.launch {
            deleteAndEditUseCase(noteId).onSuccess {
                _openNoteEditorEvent.tryEmit(it)
            }.onFailure {
                logger.error("削除に失敗しました", it)
                if (userActionAppGlobalErrorStore.dispatchAndAwaitUserAction(
                        AppGlobalError(
                            tag = "NotesViewModel.removeAndEditNote",
                            level = AppGlobalError.ErrorLevel.Error,
                            message = StringSource("Delete note failed"),
                            throwable = it,
                            retryable = true
                        ),
                        UserActionAppGlobalErrorAction.Type.Retry
                    )
                ) {
                    removeAndEditNote(
                        noteId
                    )
                }
            }
        }

    }

    fun vote(noteId: Note.Id?, poll: Poll?, choice: Poll.Choice?) {
        if (noteId == null || poll == null || choice == null) {
            return
        }
        viewModelScope.launch {
            voteUseCase(noteId, choice).onFailure {
                logger.error("投票に失敗しました", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.vote",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Vote failed"),
                        throwable = it
                    )
                )
            }.onSuccess {
                logger.debug("投票に成功しました")
            }
        }
    }

    fun onToggleFavoriteUseCase(note: Note) {
        viewModelScope.launch {
            toggleFavoriteUseCase(note.id).onFailure {
                logger.error("favoriteに失敗", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.onToggleFavoriteUseCase",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Toggle favorite failed"),
                        throwable = it
                    )
                )
            }
        }
    }

    fun translate(noteId: Note.Id) {
        viewModelScope.launch {
            translationStore.translate(noteId)
        }
    }

    fun neverShowSensitiveMediaDialog() {
        viewModelScope.launch {
            configRepository.get().mapCancellableCatching {
                it.copy(isShowWarningDisplayingSensitiveMedia = false)
            }.mapCancellableCatching {
                configRepository.save(it)
            }.onFailure {
                logger.error("警告表示の抑制に失敗", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        tag = "NotesViewModel.neverShowSensitiveMediaDialog",
                        level = AppGlobalError.ErrorLevel.Warning,
                        message = StringSource("Never show sensitive media dialog failed"),
                        throwable = it
                    )
                )
            }
        }
    }
}