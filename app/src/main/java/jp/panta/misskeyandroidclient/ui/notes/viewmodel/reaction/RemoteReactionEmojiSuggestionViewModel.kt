package jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.CreateReaction
import net.pantasystem.milktea.model.notes.reaction.Reaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RemoteReaction(
    val reaction: net.pantasystem.milktea.model.notes.reaction.Reaction,
    val currentAccountId: Long,
    val noteId: String
)

@HiltViewModel
class RemoteReactionEmojiSuggestionViewModel @Inject constructor(
    val metaRepository: net.pantasystem.milktea.model.instance.MetaRepository,
    val accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
    val noteRepository: net.pantasystem.milktea.model.notes.NoteRepository,
    val loggerFactory: net.pantasystem.milktea.common.Logger.Factory,
) : ViewModel() {

    private val _reaction = MutableStateFlow<RemoteReaction?>(null)
    val reaction: StateFlow<RemoteReaction?> = _reaction
    val logger = loggerFactory.create("RemoteReactionEmojiSuggestionVM")

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredEmojis = reaction.flatMapLatest { remoteReaction ->
        val name = remoteReaction?.reaction?.getName()
        if (name == null) {
            flow {
                emit(net.pantasystem.milktea.common.State.Fixed<List<net.pantasystem.milktea.model.emoji.Emoji>>(net.pantasystem.milktea.common.StateContent.NotExist()))
            }
        } else {
            suspend {
                val account = accountRepository.get(remoteReaction.currentAccountId)
                metaRepository.get(account.instanceDomain)?.emojis?.filter {
                    it.name == name
                }
            }.asLoadingStateFlow()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, net.pantasystem.milktea.common.State.Loading(
        net.pantasystem.milktea.common.StateContent.NotExist()))

    val isLoading = filteredEmojis.map {
        it is net.pantasystem.milktea.common.State.Loading
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isNotExists = filteredEmojis.map {
        it.content is net.pantasystem.milktea.common.StateContent.NotExist && it is net.pantasystem.milktea.common.State.Fixed
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)


    fun setReaction(accountId: Long, reaction: String, noteId: String) {
        _reaction.value = RemoteReaction(
            net.pantasystem.milktea.model.notes.reaction.Reaction(
                reaction
            ), accountId, noteId)
    }

    fun send() {
        val value = reaction.value?: return
        val name = value.reaction.getName()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                noteRepository.toggleReaction(
                    net.pantasystem.milktea.model.notes.reaction.CreateReaction(
                        net.pantasystem.milktea.model.notes.Note.Id(
                            value.currentAccountId,
                            value.noteId
                        ),
                        ":$name:"
                    )
                )
            }.onFailure {
                logger.warning("リアクションの作成失敗", e = it)
            }
        }

    }
}