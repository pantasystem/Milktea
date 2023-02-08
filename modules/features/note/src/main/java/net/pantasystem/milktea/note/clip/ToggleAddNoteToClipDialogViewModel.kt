package net.pantasystem.milktea.note.clip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
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
        ResultState.Loading(StateContent.NotExist())
    )



    private val _relatedClips = MutableStateFlow<List<Clip>>(emptyList())

    private val clipStatuses: StateFlow<ResultState<List<ClipWithAddedState>>> =
        combine(myClipsState, _relatedClips) { own, related ->
            own.convert { list ->
                list.map { clip ->
                    ClipWithAddedState(
                        clip,
                        isAdded = related.any {
                            it.id == clip.id
                        }
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResultState.Loading(StateContent.NotExist())
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
    }

    fun onAddToClip(noteId: Note.Id, clipId: ClipId) {
        viewModelScope.launch {
            clipRepository.appendNote(clipId, noteId).onSuccess {

            }.onFailure {
                logger.error("クリップへの追加に失敗", it)
            }
            loadRelatedClips(noteId)
        }
    }

    fun onRemoveToClip(noteId: Note.Id, clipId: ClipId) {
        viewModelScope.launch {
            clipRepository.removeNote(clipId, noteId).onSuccess {

            }.onFailure {
                logger.error("クリップからの削除に失敗", it)
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
    val clips: ResultState<List<ClipWithAddedState>> = ResultState.Loading(StateContent.NotExist()),
)

data class ClipWithAddedState(
    val clip: Clip,
    val isAdded: Boolean,
)