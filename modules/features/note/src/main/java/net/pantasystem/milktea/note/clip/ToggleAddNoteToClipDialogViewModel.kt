package net.pantasystem.milktea.note.clip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.model.clip.Clip
import net.pantasystem.milktea.model.clip.ClipId
import net.pantasystem.milktea.model.clip.ClipRepository
import net.pantasystem.milktea.model.notes.Note
import javax.inject.Inject

@HiltViewModel
class ToggleAddNoteToClipDialogViewModel @Inject constructor(
    private val clipRepository: ClipRepository,
    savedStateHandle: SavedStateHandle,
    private val loggerFactory: Logger.Factory,
) : ViewModel() {

    companion object {
        const val EXTRA_NOTE_ID = "ToggleAddNoteToClipDialogViewModel.EXTRA_NOTE_ID"
    }

    private val logger by lazy {
        loggerFactory.create("TANTDViewModel")
    }

    val noteId = savedStateHandle.getStateFlow<Note.Id?>(EXTRA_NOTE_ID, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val myClipsState = noteId.filterNotNull().distinctUntilChanged().flatMapLatest {
        suspend {
            clipRepository.getMyClips(it.accountId).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.initialState(),
    )


    private val _relatedClips = MutableStateFlow<List<Clip>>(emptyList())

    private val clipActionState = MutableStateFlow<Map<ClipId, ClipOptionalActionState>>(mapOf())
    private val clipActionStateQueue =
        MutableSharedFlow<Pair<ClipId, ClipOptionalActionState>>(extraBufferCapacity = 20)

    private val clipStatuses: StateFlow<ResultState<List<ClipWithAddedState>>> =
        combine(myClipsState, _relatedClips, clipActionState) { own, related, optionStatuses ->

            own.convert { list ->
                list.map { clip ->
                    val status = optionStatuses.get(clip.id)
                    val isAdded = related.any {
                        it.id == clip.id
                    }
                    ClipWithAddedState(
                        clip,
                        if (clip.isPublic) {
                            if (status is ClipOptionalActionState.Adding || status is ClipOptionalActionState.Removing) {
                                ClipAddState.Progress
                            } else if (isAdded) {
                                ClipAddState.Added
                            } else {
                                ClipAddState.NotAdded
                            }
                        } else {
                            when(status) {
                                is ClipOptionalActionState.Added -> ClipAddState.Added
                                is ClipOptionalActionState.Adding -> ClipAddState.Progress
                                is ClipOptionalActionState.AlreadyAdded -> ClipAddState.Added
                                is ClipOptionalActionState.AlreadyRemoved -> ClipAddState.NotAdded
                                is ClipOptionalActionState.Removed -> ClipAddState.NotAdded
                                is ClipOptionalActionState.Removing -> ClipAddState.Progress
                                null -> ClipAddState.Unknown
                            }
                        }
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResultState.initialState(),
        )

    val uiState = combine(noteId, clipStatuses) { noteId, clipStatuses ->
        ToggleAddNoteToClipDialogUiState(
            noteId,
            clipStatuses
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ToggleAddNoteToClipDialogUiState()
    )

    init {
        noteId.filterNotNull().onEach {
            loadRelatedClips(it)
        }.launchIn(viewModelScope)

        clipActionStateQueue.onEach { (clipId, state) ->
            clipActionState.value = clipActionState.value.toMutableMap().also { map ->
                map[clipId] = state
            }
        }.launchIn(viewModelScope)
    }

    fun onAddToClip(noteId: Note.Id, clip: Clip) {
        viewModelScope.launch {
            clipActionStateQueue.emit(clip.id to ClipOptionalActionState.Adding(clip, noteId))
            clipRepository.appendNote(clip.id, noteId).onSuccess {
                clipActionStateQueue.emit(clip.id to ClipOptionalActionState.Added(clip, noteId))
            }.onFailure {
                logger.error("クリップへの追加に失敗", it)
                if (it is APIError.ClientException) {
                    clipActionStateQueue.emit(
                        clip.id to ClipOptionalActionState.AlreadyAdded(
                            clip,
                            noteId
                        )
                    )
                }
            }
            loadRelatedClips(noteId)
        }
    }

    fun onRemoveToClip(noteId: Note.Id, clip: Clip) {
        viewModelScope.launch {
            clipActionStateQueue.emit(clip.id to ClipOptionalActionState.Removing(clip, noteId))
            clipRepository.removeNote(clip.id, noteId).onSuccess {
                clipActionStateQueue.emit(clip.id to ClipOptionalActionState.Removed(clip, noteId))
            }.onFailure {
                logger.error("クリップからの削除に失敗", it)
                if (it is APIError.ClientException) {
                    clipActionStateQueue.emit(clip.id to ClipOptionalActionState.AlreadyRemoved(clip, noteId))
                }
            }
            loadRelatedClips(noteId)
        }
    }

    private suspend fun loadRelatedClips(noteId: Note.Id) {
        clipRepository.findBy(noteId).onSuccess {
            _relatedClips.value = it
        }.onFailure {
            logger.error("関連しているクリップの取得に失敗", it)
        }
    }

}

data class ToggleAddNoteToClipDialogUiState(
    val noteId: Note.Id? = null,
    val clips: ResultState<List<ClipWithAddedState>> = ResultState.initialState(),
)

data class ClipWithAddedState(
    val clip: Clip,
    val addState: ClipAddState,
)

sealed interface ClipOptionalActionState {
    data class Adding(val clip: Clip, val noteId: Note.Id) : ClipOptionalActionState
    data class Removing(val clip: Clip, val noteId: Note.Id) : ClipOptionalActionState
    data class Added(val clip: Clip, val noteId: Note.Id) : ClipOptionalActionState
    data class Removed(val clip: Clip, val noteId: Note.Id) : ClipOptionalActionState
    data class AlreadyAdded(val clip: Clip, val noteId: Note.Id) : ClipOptionalActionState
    data class AlreadyRemoved(val clip: Clip, val noteId: Note.Id) : ClipOptionalActionState
}

sealed interface ClipAddState {
    object Added : ClipAddState
    object NotAdded : ClipAddState
    object Unknown : ClipAddState
    object Progress : ClipAddState
}