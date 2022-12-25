package net.pantasystem.milktea.note.reaction.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.reaction.viewmodel.EmojiType
import net.pantasystem.milktea.note.reaction.viewmodel.from


class ReactionHistoryViewModel @AssistedInject constructor(
    reactionHistoryDataSource: ReactionHistoryDataSource,
    paginatorFactory: ReactionHistoryPaginator.Factory,
    val loggerFactory: Logger.Factory,
    val metaRepository: MetaRepository,
    val accountRepository: AccountRepository,
    val noteRepository: NoteRepository,
    @Assisted val noteId: Note.Id,
    @Assisted val type: String?
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(noteId: Note.Id, type: String?): ReactionHistoryViewModel
    }

    companion object

    val logger = loggerFactory.create("ReactionHistoryVM")

    val isLoading = MutableStateFlow(false)
    val histories = MutableStateFlow<List<ReactionHistory>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val emojis = flowOf(noteId).mapNotNull {
        accountRepository.get(it.accountId).getOrNull()
    }.flatMapLatest {
        metaRepository.observe(it.normalizedInstanceDomain)
    }.map {
        it?.emojis ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val note = noteRepository.observeOne(noteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val paginator = paginatorFactory.create(ReactionHistoryRequest(noteId, type))

    val uiState = combine(emojis, note, isLoading, histories) { emojis, note, loading, histories ->
        ReactionHistoryUiState(
            items = listOfNotNull(
                type?.let { type ->
                    EmojiType.from(emojis + (note?.emojis ?: emptyList()), type)?.let {
                        ReactionHistoryListType.Header(it)
                    }
                }
            ) + histories.map {
                ReactionHistoryListType.ItemUser(it.user)
            } + listOfNotNull(
                if (loading) {
                    ReactionHistoryListType.Loading
                } else {
                    null
                }
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReactionHistoryUiState(listOf())
    )

    init {
        reactionHistoryDataSource.filter(noteId, type).onEach {
            histories.value = it
        }.catch {

        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun next() {
        if (isLoading.value) {
            return
        }
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {

            runCatching {
                paginator.next()
            }.onFailure {
                logger.error("リアクションの履歴の取得に失敗しました", e = it)
            }
            isLoading.value = false
        }
    }

}

@Suppress("UNCHECKED_CAST")
fun ReactionHistoryViewModel.Companion.provideViewModel(
    factory: ReactionHistoryViewModel.ViewModelAssistedFactory,
    noteId: Note.Id,
    type: String?
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(noteId, type) as T
    }
}

sealed interface ReactionHistoryListType {
    data class ItemUser(val user: User) : ReactionHistoryListType
    data class Header(val emojiType: EmojiType) : ReactionHistoryListType
    object Loading : ReactionHistoryListType
}

data class ReactionHistoryUiState(
    val items: List<ReactionHistoryListType> = emptyList(),

    )