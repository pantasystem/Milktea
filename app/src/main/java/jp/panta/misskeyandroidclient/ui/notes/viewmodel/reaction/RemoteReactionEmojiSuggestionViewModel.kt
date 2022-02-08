package jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import jp.panta.misskeyandroidclient.model.notes.reaction.Reaction
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.util.asLoadingStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class RemoteReaction(
    val reaction: Reaction,
    val currentAccountId: Long
)

@HiltViewModel
class RemoteReactionEmojiSuggestionViewModel @Inject constructor(
    val metaRepository: MetaRepository,
    val accountRepository: AccountRepository,
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val metaRepository: MetaRepository,
        val accountRepository: AccountRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RemoteReactionEmojiSuggestionViewModel(metaRepository, accountRepository) as T
        }
    }
    private val _reaction = MutableStateFlow<RemoteReaction?>(null)
    val reaction: StateFlow<RemoteReaction?> = _reaction

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredEmojis = reaction.flatMapLatest { remoteReaction ->
        val name = remoteReaction?.reaction?.getName()
        if (name == null) {
            flow {
                emit(State.Fixed<List<Emoji>>(StateContent.NotExist()))
            }
        } else {
            suspend {
                val account = accountRepository.get(remoteReaction.currentAccountId)
                metaRepository.get(account.instanceDomain)?.emojis?.filter {
                    it.name == name
                }
            }.asLoadingStateFlow()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, State.Loading(StateContent.NotExist()))

    val isLoading = filteredEmojis.map {
        it is State.Loading
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isNotExists = filteredEmojis.map {
        it.content is StateContent.NotExist && it is State.Fixed
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)


    fun setReaction(accountId: Long, reaction: String) {
        _reaction.value = RemoteReaction(Reaction(reaction), accountId)
    }
}