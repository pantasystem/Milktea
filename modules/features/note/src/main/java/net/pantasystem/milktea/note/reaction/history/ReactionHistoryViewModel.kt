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
import net.pantasystem.milktea.common_android.emoji.V13EmojiUrlResolver
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRepository
import net.pantasystem.milktea.model.note.reaction.*
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.note.EmojiType
import net.pantasystem.milktea.note.from


class ReactionHistoryViewModel @AssistedInject constructor(
    loggerFactory: Logger.Factory,
    private val accountRepository: AccountRepository,
    noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val reactionUserRepository: ReactionUserRepository,
    private val customEmojiRepository: CustomEmojiRepository,
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

    private val users = reactionUserRepository.observeBy(noteId, type)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val emojis = flowOf(noteId).mapNotNull {
        accountRepository.get(it.accountId).getOrNull()
    }.flatMapLatest {
        customEmojiRepository.observeBy(it.getHost())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val note = noteRepository.observeOne(noteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(FlowPreview::class)
    private val account = suspend {
        accountRepository.get(noteId.accountId).getOrNull()
    }.asFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val noteAuthor = note.filterNotNull().map {
        userRepository.find(it.userId)
    }.catch {
        logger.error("ノートの作者情報の取得に失敗", it)
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
        users,
        account,
    ) { noteInfo, emojis, loading, users, a ->
        ReactionHistoryUiState(
            items = listOfNotNull(
                type?.let { type ->
                    EmojiType.from(emojis + (noteInfo.note?.emojis ?: emptyList()), type)
                        ?: EmojiType.CustomEmoji(
                            CustomEmoji(
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
            ) + users.map {
                ReactionHistoryListType.ItemUser(it, account = a)
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
        viewModelScope.launch {
            isLoading.value = true
            reactionUserRepository.syncBy(noteId, type).onFailure {
                logger.error("リアクション履歴の同期に失敗", it)
            }
            isLoading.value = false
        }
    }

    fun next() {

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