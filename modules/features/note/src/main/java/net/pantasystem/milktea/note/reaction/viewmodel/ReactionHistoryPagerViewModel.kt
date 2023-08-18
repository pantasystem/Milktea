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
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteRepository
import net.pantasystem.milktea.model.note.reaction.ReactionHistoryRequest
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReactionHistoryPagerViewModel @Inject constructor(
    val noteRepository: NoteRepository,
    val adapter: NoteCaptureAPIAdapter,
    val noteDataSource: NoteDataSource,
    val userRepository: UserRepository,
    val accountRepository: AccountRepository,
    val loggerFactory: Logger.Factory,
) : ViewModel() {

    val logger by lazy {
        loggerFactory.create("ReactionHistoryVM")
    }


    private val noteId = MutableStateFlow<Note.Id?>(null)
    val note: StateFlow<Note?> = noteId.filterNotNull().flatMapLatest {
        noteDataSource.observeOne(it)
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

    private val account = noteId.filterNotNull().map {
        accountRepository.get(it.accountId).getOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val author = note.filterNotNull().map {
        userRepository.find(it.userId)
    }.catch {
        logger.error("投稿したユーザの取得に失敗", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val uiState = combine(note, account, author, types) { note, ac, author, types ->
        ReactionHistoryPagerUiState(note, ac, author,types)
    }.stateIn(viewModelScope, SharingStarted.Lazily, ReactionHistoryPagerUiState())

    init {
        viewModelScope.launch {
            noteId.filterNotNull().flatMapLatest { noteId ->
                adapter.capture(noteId)
            }.flowOn(Dispatchers.IO).collect()
        }
        viewModelScope.launch {
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
    val account: Account? = null,
    val noteAuthor: User? = null,
    val types: List<ReactionHistoryRequest> = emptyList()
)