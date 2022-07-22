package jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.instance.MetaRepository
import javax.inject.Inject

@HiltViewModel
class ReactionSelectionDialogViewModel @Inject constructor(
    val accountStore: AccountStore,
    val metaRepository: MetaRepository,
) : ViewModel() {

    val searchWord = MutableStateFlow("")

    val isSearchMode = searchWord.map {
        it.isNotBlank()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)


    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredEmojis = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        metaRepository.observe(ac.instanceDomain)
    }.filterNotNull().mapNotNull {
        it.emojis
    }.flatMapLatest { emojis ->
        searchWord.map { word ->
            word.replace(":", "")
        }.map { word ->
            emojis.filter { emoji ->
                emoji.name.startsWith(word)
                        || emoji.aliases?.any { alias ->
                    alias.startsWith(word)
                } ?: false
            }
        }
    }.map { emojis ->
        emojis.map {
            ":${it.name}:"
        }
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

}

