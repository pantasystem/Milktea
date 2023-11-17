package jp.panta.misskeyandroidclient.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class TabViewModel @Inject constructor(
    val accountStore: AccountStore,
    private val userRepository: UserRepository,
    private val configRepository: LocalConfigRepository,
    loggerFactory: Logger.Factory,
): ViewModel() {

    private val logger by lazy {
        loggerFactory.create("TabViewModel")
    }

    val pages = accountStore.observeCurrentAccount.map {
        it?.pages ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
        userRepository.observe(User.Id(it.accountId, it.remoteId))
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val visibleInstanceInfo = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
        configRepository.observe().map {
            if (it.isVisibleInstanceUrlInToolbar) {
                CurrentAccountInstanceInfoUrl.Visible(account.getHost())
            } else {
                CurrentAccountInstanceInfoUrl.Invisible
            }
        }
    }.catch {
        logger.error("observe account, config error", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CurrentAccountInstanceInfoUrl.Visible(""))

    val avatarIconShapeType = configRepository.observe().map {
        it.avatarIconShapeType
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DefaultConfig.config.avatarIconShapeType)
}

sealed interface CurrentAccountInstanceInfoUrl {
    data object Invisible : CurrentAccountInstanceInfoUrl
    data class Visible(val host: String) : CurrentAccountInstanceInfoUrl
}