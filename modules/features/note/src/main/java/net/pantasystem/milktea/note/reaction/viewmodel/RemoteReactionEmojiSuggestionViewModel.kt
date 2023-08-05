package net.pantasystem.milktea.note.reaction.viewmodel

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
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ToggleReactionUseCase
import javax.inject.Inject

data class RemoteReaction(
    val reaction: Reaction,
    val currentAccountId: Long,
    val noteId: String
)

@HiltViewModel
class RemoteReactionEmojiSuggestionViewModel @Inject constructor(
    val accountRepository: AccountRepository,
    val noteRepository: NoteRepository,
    val customEmojiRepository: CustomEmojiRepository,
    val loggerFactory: Logger.Factory,
    val toggleReactionUseCase: ToggleReactionUseCase,
) : ViewModel() {

    private val _reaction = MutableStateFlow<RemoteReaction?>(null)
    val reaction: StateFlow<RemoteReaction?> = _reaction
    val logger = loggerFactory.create("RemoteReactionEmojiSuggestionVM")

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredEmojis = reaction.flatMapLatest { remoteReaction ->
        val name = remoteReaction?.reaction?.getName()
        if (name == null) {
            flow {
                emit(ResultState.Fixed<List<Emoji>>(StateContent.NotExist()))
            }
        } else {
            suspend {
                val account = accountRepository.get(remoteReaction.currentAccountId).getOrThrow()
                customEmojiRepository.findBy(account.getHost()).getOrThrow().filter {
                    it.name == name
                }
            }.asLoadingStateFlow()
        }
    }.stateIn(
        viewModelScope, SharingStarted.Lazily, ResultState.initialState()
    )

    fun setReaction(accountId: Long, reaction: String, noteId: String) {
        _reaction.value = RemoteReaction(
            Reaction(
                reaction
            ), accountId, noteId
        )
    }

    fun send() {
        val value = reaction.value ?: return
        val name = value.reaction.getName()
        viewModelScope.launch {
            toggleReactionUseCase(
                Note.Id(
                    value.currentAccountId,
                    value.noteId
                ),
                ":$name:"
            ).onFailure {
                logger.warning("リアクションの作成失敗", e = it)
            }
        }

    }
}