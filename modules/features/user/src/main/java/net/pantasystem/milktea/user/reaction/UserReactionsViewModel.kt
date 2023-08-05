package net.pantasystem.milktea.user.reaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.user.UserReactionPagingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import javax.inject.Inject

@HiltViewModel
class UserReactionsViewModel @Inject constructor(
    accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    storeFactory: UserReactionPagingStore.Factory,
    loggerFactory: Logger.Factory,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    private val savedStateHandle: SavedStateHandle,
    configRepository: LocalConfigRepository,
) : ViewModel() {
    //    private val _userId = MutableStateFlow<User.Id?>(null)
    companion object {
        const val ACCOUNT_ID = "accountId"
        const val USER_ID = "userId"
    }
    val logger = loggerFactory.create("UserReactionsViewModel")

    private val userId by lazy {
        User.Id(
            accountId = savedStateHandle[ACCOUNT_ID]!!,
            id = savedStateHandle[USER_ID]!!
        )
    }

    private val currentAccountWatcher = CurrentAccountWatcher(
        accountRepository = accountRepository,
        currentAccountId = null
    )

    private val cache = planeNoteViewDataCacheFactory.create(currentAccountWatcher::getAccount, viewModelScope)

    private val config = configRepository.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),  DefaultConfig.config)


    private val store = storeFactory.create(userId)
    val state = store.state.map { pageableState ->
        pageableState.suspendConvert { list ->
            list.map {
                UserReactionBindingModel(
                    reaction = it.reaction,
                    user = userRepository.observe(it.user.id)
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), it.user),
                    note = cache.get(it.note),
                    config = config,
                )
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init()
    )

    init {
        loadPrevious()
    }

    fun loadPrevious() {
        viewModelScope.launch {
            store.loadPrevious().onFailure {
                logger.error("loadPrevious error", it)
            }
        }
    }

    fun clearAndLoadPrevious() {
        viewModelScope.launch {
            store.clear()
            loadPrevious()
        }
    }
}