package net.pantasystem.milktea.clip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.watchAccount
import net.pantasystem.milktea.model.clip.Clip
import net.pantasystem.milktea.model.clip.ClipRepository
import javax.inject.Inject

@HiltViewModel
class ClipListViewModel @Inject constructor(
    private val clipRepository: ClipRepository,
    private val accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId = savedStateHandle.getStateFlow<Long>(
        ClipListNavigationImpl.EXTRA_ACCOUNT_ID, -1L
    ).map {
        it.takeIf {
            it > 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccount = accountId.map {
        if (it == null) {
            accountRepository.getCurrentAccount().getOrNull()
        } else {
            accountRepository.get(it).getOrNull()
        }
    }.filterNotNull().flatMapLatest {
        accountRepository.watchAccount(it.accountId)
    }.catch {

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val clips = currentAccount.filterNotNull().map {
        it.accountId
    }.distinctUntilChanged().flatMapLatest { accountId ->
        suspend {
            clipRepository.getMyClips(accountId).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Loading(StateContent.NotExist())
    )

    private val clipItemStatuses = combine(clips, currentAccount) { clipsState, account ->
        clipsState.convert { clips ->
            clips.map { clip ->
                val isAddedToTab = account?.pages?.any {
                    clip.id.clipId == it.pageParams.clipId
                }
                ClipItemState(clip, isAddedToTab ?: false)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Loading(StateContent.NotExist())
    )

    val uiState = combine(currentAccount, clipItemStatuses) { ac, statuses ->
        ClipListUiState(
            ac,
            statuses
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ClipListUiState()
    )

    fun onToggleAddToTabButtonClicked(clipItemState: ClipItemState) {

    }

    fun onClipTileClicked(clipItemState: ClipItemState) {

    }
}

data class ClipItemState(
    val clip: Clip,
    val isAddedToTab: Boolean,
)

data class ClipListUiState(
    val account: Account? = null,
    val clipStatusesState: ResultState<List<ClipItemState>> = ResultState.Loading(StateContent.NotExist()),
)