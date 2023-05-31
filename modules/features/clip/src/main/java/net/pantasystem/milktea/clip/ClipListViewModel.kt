package net.pantasystem.milktea.clip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common_navigation.ClipListNavigationArgs
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.clip.Clip
import net.pantasystem.milktea.model.clip.ClipRepository
import net.pantasystem.milktea.model.clip.ToggleClipAddToTabUseCase
import javax.inject.Inject

@HiltViewModel
class ClipListViewModel @Inject constructor(
    loggerFactory: Logger.Factory,
    private val clipRepository: ClipRepository,
    private val accountStore: AccountStore,
    private val toggleClipAddToTabUseCase: ToggleClipAddToTabUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val logger by lazy(LazyThreadSafetyMode.NONE) {
        loggerFactory.create("ClipListViewModel")
    }

    private val accountId = savedStateHandle.getStateFlow<Long>(
        ClipListNavigationImpl.EXTRA_ACCOUNT_ID, -1L
    ).map {
        it.takeIf {
            it > 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccount = accountId.flatMapLatest { accountId ->
        accountStore.state.map { state ->
            accountId?.let {
                state.get(it)
            } ?: state.currentAccount
        }
    }.catch {

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val clips = currentAccount.filterNotNull().map {
        it.accountId
    }.distinctUntilChanged().flatMapLatest { accountId ->
        suspend {
            clipRepository.getMyClips(accountId).onFailure {
                logger.error("getClips failed: $it", it)
            }.getOrThrow()
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
        viewModelScope.launch {
            toggleClipAddToTabUseCase(clipItemState.clip)
        }
    }

    fun onClipTileClicked(clipItemState: ClipItemState) {
        val mode = savedStateHandle.get<String?>(ClipListNavigationImpl.EXTRA_MODE)?.let {
            ClipListNavigationArgs.Mode.valueOf(it)
        } ?: ClipListNavigationArgs.Mode.View
        when(mode) {
            ClipListNavigationArgs.Mode.AddToTab -> {
                onToggleAddToTabButtonClicked(clipItemState)
            }
            ClipListNavigationArgs.Mode.View -> Unit
        }
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