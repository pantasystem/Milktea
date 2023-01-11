package net.pantasystem.milktea.note.reaction.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.emoji.V13EmojiUrlResolver
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.note.reaction.viewmodel.EmojiType
import net.pantasystem.milktea.note.reaction.viewmodel.from


class ReactionHistoryViewModel @AssistedInject constructor(
    reactionHistoryDataSource: ReactionHistoryDataSource,
    paginatorFactory: ReactionHistoryPaginator.Factory,
    val loggerFactory: Logger.Factory,
    val metaRepository: MetaRepository,
    val accountRepository: AccountRepository,
    val noteRepository: NoteRepository,
    val userRepository: UserRepository,
    @Assisted val noteId: Note.Id,
    @Assisted val type: String?
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(noteId: Note.Id, type: String?): ReactionHistoryViewModel
    }

    companion object

    val logger = loggerFactory.create("ReactionHistoryVM")

    private val isLoading = MutableStateFlow(false)
    private val histories = MutableStateFlow<List<ReactionHistory>>(emptyList())

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

    @OptIn(FlowPreview::class)
    private val account = suspend {
        accountRepository.get(noteId.accountId).getOrNull()
    }.asFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val noteAuthor = note.filterNotNull().map {
        userRepository.find(it.userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val noteInfo = combine(note, noteAuthor) { n, author ->
        NoteInfo(
            note = n,
            user = author,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteInfo())
    val uiState = combine(
        noteInfo,
        emojis,
        isLoading,
        histories,
        account,
    ) { noteInfo, emojis, loading, histories, a ->
        ReactionHistoryUiState(
            items = listOfNotNull(
                type?.let { type ->
                    EmojiType.from(emojis + (noteInfo.note?.emojis ?: emptyList()), type)
                        ?: EmojiType.CustomEmoji(
                            Emoji(
                                name = type,
                                url = V13EmojiUrlResolver.resolve(
                                    accountHost = a?.getHost(),
                                    emojiHost = noteInfo.user?.host,
                                    tagName = type
                                ),
                                uri = V13EmojiUrlResolver.resolve(
                                    accountHost = a?.getHost(),
                                    emojiHost = noteInfo.user?.host,
                                    tagName = type
                                )
                            )
                        )
                    EmojiType.from(emojis + (noteInfo.note?.emojis ?: emptyList()), type)?.let {
                        ReactionHistoryListType.Header(it)
                    }
                }
            ) + histories.map {
                ReactionHistoryListType.ItemUser(it.user, account = a)
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
        viewModelScope.launch {

            runCancellableCatching {
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
    data class ItemUser(val user: User, val account: Account?) : ReactionHistoryListType
    data class Header(val emojiType: EmojiType) : ReactionHistoryListType
    object Loading : ReactionHistoryListType
}

data class ReactionHistoryUiState(
    val items: List<ReactionHistoryListType> = emptyList(),

    )


private data class NoteInfo(
    val note: Note? = null,
    val user: User? = null
)