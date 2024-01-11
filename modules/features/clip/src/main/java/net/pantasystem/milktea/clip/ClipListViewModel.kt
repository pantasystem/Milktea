package net.pantasystem.milktea.clip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.handler.AppGlobalError
import net.pantasystem.milktea.app_store.handler.UserActionAppGlobalErrorAction
import net.pantasystem.milktea.app_store.handler.UserActionAppGlobalErrorStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.common_android.resource.StringSource
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
    private val savedStateHandle: SavedStateHandle,
    private val userActionAppGlobalErrorStore: UserActionAppGlobalErrorStore,
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

    private val addTabToAccountId = savedStateHandle.getStateFlow<Long>(
        ClipListNavigationImpl.EXTRA_ADD_TAB_TO_ACCOUNT_ID,
        -1
    ).map {
        it.takeIf {
            it > 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccount = accountId.flatMapLatest { accountId ->
        accountStore.getOrCurrent(accountId)
    }.catch {
        logger.error("currentAccount failed: $it", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val addTabToAccount = addTabToAccountId.flatMapLatest {
        accountStore.getOrCurrent(it)
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
        ResultState.initialState(),
    )

    private val clipItemStatuses = combine(
        clips,
        addTabToAccount,
        currentAccount
    ) { clipsState, addTabToAccount, account ->
        clipsState.convert { clips ->
            clips.map { clip ->
                val isAddedToTab = (addTabToAccount ?: account)?.pages?.any {
                    clip.id.clipId == it.pageParams.clipId
                            && (it.attachedAccountId ?: it.accountId) == account?.accountId
                }
                ClipItemState(clip, isAddedToTab ?: false)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.initialState()
    )

    val uiState = combine(
        currentAccount,
        addTabToAccount,
        clipItemStatuses
    ) { ac, addTabToAccount, statuses ->
        ClipListUiState(
            ac, addTabToAccount, statuses
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), ClipListUiState()
    )

    fun onToggleAddToTabButtonClicked(clipItemState: ClipItemState) {
        viewModelScope.launch {
            toggleClipAddToTabUseCase(
                clipItemState.clip,
                savedStateHandle[ClipListNavigationImpl.EXTRA_ADD_TAB_TO_ACCOUNT_ID]
            ).onFailure {
                if (userActionAppGlobalErrorStore.dispatchAndAwaitUserAction(
                        AppGlobalError(
                            "ClipListViewModel.onToggleAddToTabButtonClicked",
                            AppGlobalError.ErrorLevel.Error,
                            StringSource.invoke("add/remove clip to tab failed"),
                            it
                        ),
                        UserActionAppGlobalErrorAction.Type.Retry
                    )
                ) {
                    onToggleAddToTabButtonClicked(clipItemState)
                }
            }
        }
    }

    fun onClipTileClicked(clipItemState: ClipItemState) {
        val mode = savedStateHandle.get<String?>(ClipListNavigationImpl.EXTRA_MODE)?.let {
            ClipListNavigationArgs.Mode.valueOf(it)
        } ?: ClipListNavigationArgs.Mode.View
        when (mode) {
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
    val addToTabAccount: Account? = null,
    val clipStatusesState: ResultState<List<ClipItemState>> = ResultState.initialState(),
)