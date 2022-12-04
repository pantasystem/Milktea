package jp.panta.misskeyandroidclient.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

@HiltViewModel
class TabViewModel @Inject constructor(
    val accountStore: AccountStore,
    private val userDataSource: UserDataSource,
): ViewModel() {
    
    val pages = accountStore.observeCurrentAccount.map {
        it?.pages ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
        userDataSource.observe(User.Id(it.accountId, it.remoteId))
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}