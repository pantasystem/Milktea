package net.pantasystem.milktea.antenna.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.antenna.AntennaRepository
import javax.inject.Inject

@HiltViewModel
class AntennaListViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val antennaRepository: AntennaRepository,
) : ViewModel() {


    val antennas = MediatorLiveData<List<Antenna>>()

    val editAntennaEvent = EventBus<Antenna>()

    val confirmDeletionAntennaEvent = EventBus<Antenna>()

    private val openAntennasTimelineEvent = EventBus<Antenna>()

    val isLoading = MutableLiveData(false)
    private var mIsLoading: Boolean = false
        set(value) {
            field = value
            isLoading.postValue(value)
        }

    private val mPagedAntennaIds = MutableLiveData<Set<Antenna.Id>>()
    val pagedAntennaIds: LiveData<Set<Antenna.Id>> = mPagedAntennaIds


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
        viewModelScope.launch {
            runCancellableCatching {
                val account = accountRepository.getCurrentAccount().getOrThrow()
                antennaRepository.findByAccountId(account.accountId).getOrThrow()
            }.onSuccess {
                antennas.postValue(it)
            }
            mIsLoading = false
        }

    }

    fun toggleTab(antenna: Antenna?) {
        antenna ?: return
        val paged = accountStore.currentAccount?.pages?.firstOrNull {

            it.pageParams.antennaId == antenna.id.antennaId
        }
        viewModelScope.launch {
            if (paged == null) {
                accountStore.addPage(
                    PageableTemplate(accountStore.currentAccount!!)
                        .antenna(
                            antenna
                        )
                )
            } else {
                accountStore.removePage(paged)
            }
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