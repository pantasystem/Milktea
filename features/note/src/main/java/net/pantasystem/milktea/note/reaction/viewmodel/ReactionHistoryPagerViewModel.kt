package net.pantasystem.milktea.note.reaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReactionHistoryPagerViewModel @Inject constructor(
    val noteRepository: NoteRepository,
    val adapter: NoteCaptureAPIAdapter,
    val noteDataSource: NoteDataSource,
    val loggerFactory: Logger.Factory,
) : ViewModel() {

    val logger by lazy {
        loggerFactory.create("ReactionHistoryVM")
    }


    private val noteId = MutableStateFlow<Note.Id?>(null)
    val note: StateFlow<Note?> = noteId.filterNotNull().flatMapLatest {
        noteDataSource.state.map { state ->
            state.getOrNull(it)
        }
    }.catch { e ->
        logger.warning("ノートの取得に失敗", e = e)
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Lazily, null)

    private val types: Flow<List<ReactionHistoryRequest>> = note.mapNotNull { note ->
        note?.id?.let {  note.id to note.reactionCounts }
    }.map { idAndList ->
        idAndList.second.map { count ->
            count.reaction
        }.map {
            ReactionHistoryRequest(idAndList.first, it)
        }
    }.shareIn(viewModelScope, SharingStarted.Eagerly)

    val uiState = combine(note, types) { note, types ->
        ReactionHistoryPagerUiState(note, types)
    }.stateIn(viewModelScope, SharingStarted.Lazily, ReactionHistoryPagerUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            noteId.filterNotNull().flatMapLatest { noteId ->
                adapter.capture(noteId)
            }.collect()
        }
        viewModelScope.launch(Dispatchers.IO) {
            noteId.filterNotNull().flatMapLatest {
                suspend {
                    noteRepository.find(it).getOrThrow()
                }.asLoadingStateFlow()
            }.collect()
        }
    }

    fun setNoteId(noteId: Note.Id) {
        this.noteId.update {
            noteId
        }
    }
}

data class ReactionHistoryPagerUiState(
    val note: Note? = null,
    val types: List<ReactionHistoryRequest> = emptyList()
)