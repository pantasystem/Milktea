package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.model.account.AccountStore
import javax.inject.Inject

@HiltViewModel
class TabViewModel @Inject constructor(
    val accountStore: AccountStore
): ViewModel() {

    val currentAccount = accountStore.observeCurrentAccount.distinctUntilChanged().filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

}