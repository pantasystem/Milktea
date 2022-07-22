package jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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



}

data class ReactionSelectionDialogUiState(
    val searchWord: String?,

) {
    fun filteredEmojis() {

    }
}
