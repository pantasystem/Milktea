package net.pantasystem.milktea.antenna.viewmodel

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.antenna.AntennaRepository
import net.pantasystem.milktea.model.antenna.AntennaToggleAddToTabUseCase
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AntennaListViewModel @Inject constructor(
    loggerFactory: Logger.Factory,
    private val accountStore: AccountStore,
    private val antennaRepository: AntennaRepository,
    private val antennaToggleAddToTabUseCase: AntennaToggleAddToTabUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_SPECIFIED_ACCOUNT_ID = "AntennaListViewModel.EXTRA_SPECIFIED_ACCOUNT_ID"
        const val EXTRA_ADD_TAB_TO_ACCOUNT_ID = "AntennaListViewModel.EXTRA_ADD_TAB_TO_ACCOUNT_ID"
    }

    private val logger by lazy(LazyThreadSafetyMode.NONE) {
        loggerFactory.create("AntennaListViewModel")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_SPECIFIED_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.state.map { state ->
            accountId?.let {
                state.get(it)
            } ?: state.currentAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val addTabToAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_ADD_TAB_TO_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.state.map { state ->
            accountId?.let { accountId ->
                state.get(accountId)
            } ?: state.currentAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val refreshAntennasEvents = MutableStateFlow(Date().time)

    @OptIn(ExperimentalCoroutinesApi::class)
    val antennasState = refreshAntennasEvents.flatMapLatest {
        currentAccount.filterNotNull().flatMapLatest { account ->
            suspend {
                Log.d("AntennaListViewModel", "antenna account state: ${account.accountId}")
                antennaRepository.findByAccountId(account.accountId).getOrThrow()
            }.asLoadingStateFlow()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.initialState()
    )

    val uiState = combine(currentAccount, addTabToAccount, antennasState) { ca, ata, state ->
        AntennaListUiState(
            currentAccount = ca,
            addTabToAccount = ata,
            antennas = state.convert { antennas ->
                antennas.map { antenna ->
                    AntennaListItem(
                        antenna = antenna,
                        isAddedToTab = (ata ?: ca)?.pages?.any { page ->
                            page.pageParams.antennaId == antenna.id.antennaId
                                    && (page.attachedAccountId ?: page.accountId) == antenna.id.accountId
                        } ?: false
                    )
                }
            }
        )
    }

    val editAntennaEvent = EventBus<Antenna>()

    val confirmDeletionAntennaEvent = EventBus<Antenna>()

    private val openAntennasTimelineEvent = EventBus<Antenna>()


    private val mPagedAntennaIds = MutableLiveData<Set<Antenna.Id>>()


    init {

        accountStore.observeCurrentAccount.onEach {
            loadInit()

            mPagedAntennaIds.postValue(
                it?.pages?.mapNotNull { page ->
                    val pageable = page.pageable()
                    if (pageable is Pageable.Antenna) {
                        it.accountId.let { accountId ->
                            Antenna.Id(accountId, pageable.antennaId)
                        }
                    } else {
                        null
                    }
                }?.toSet() ?: emptySet()
            )
        }.launchIn(viewModelScope + Dispatchers.IO)


    }

    private val deleteResultEvent = EventBus<Boolean>()

    fun loadInit() {
        refreshAntennasEvents.tryEmit(Date().time)
    }

    fun toggleTab(antenna: Antenna?) = viewModelScope.launch {
        antenna ?: return@launch
        runCancellableCatching {
            antennaToggleAddToTabUseCase(antenna, savedStateHandle[EXTRA_ADD_TAB_TO_ACCOUNT_ID])
        }.onFailure {
            logger.error("Failed to toggle tab", it)
        }
    }

    fun confirmDeletionAntenna(antenna: Antenna?) {
        antenna ?: return
        confirmDeletionAntennaEvent.event = antenna
    }

    fun editAntenna(antenna: Antenna?) {
        antenna ?: return
        editAntennaEvent.event = antenna
    }

    fun openAntennasTimeline(antenna: Antenna?) {
        openAntennasTimelineEvent.event = antenna
    }

    fun deleteAntenna(antenna: Antenna) {
        viewModelScope.launch {
            antennaRepository.delete(antenna.id).onSuccess {
                loadInit()
            }.onFailure {
                deleteResultEvent.event = false
            }
        }
    }
}

data class AntennaListItem(
    val antenna: Antenna,
    val isAddedToTab: Boolean,
)

data class AntennaListUiState(
    val currentAccount: Account?,
    val addTabToAccount: Account?,
    val antennas: ResultState<List<AntennaListItem>>,
)