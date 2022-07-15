package jp.panta.misskeyandroidclient.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.query.FindUsersQuery
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    val accountStore: AccountStore,
    val userDataSource: UserDataSource,
    val userRepository: UserRepository
): ViewModel() {
    private val findUsers = MutableStateFlow<List<ExploreItem>>(emptyList())

    val uiState = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        findUsers.map { list ->
            list.map {
                it to suspend {
                    it to userRepository.findUsers(ac.accountId, it.findUsersQuery)
                }.asLoadingStateFlow()
            }
        }
    }.flatMapLatest { exploreItems ->
        userDataSource.state.distinctUntilChangedBy {
            it.usersMap.keys.toSet()
        }.flatMapLatest { usersState ->
            combine(exploreItems.map { it.second }) { states ->
                states.toList()
            }.map { list ->
                require(list.size == exploreItems.size) {
                    "受け取った結果と, 入力値のサイズが異なります"
                }
                list.mapIndexed { index, resultState ->
                    val exploreItem = exploreItems[index].first
                    ExploreItemState(
                        findUsersQuery = exploreItem.findUsersQuery,
                        loadingState = resultState.suspendConvert {
                            it.second.map { user ->
                                usersState.get(user.id) as User.Detail
                            }
                        },
                        title = exploreItem.title,
                    )
                }
            }
        }
    }.map { list ->
        ExploreUiState(list)
    }.catch { e ->
        FirebaseCrashlytics.getInstance().recordException(e)
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Lazily, ExploreUiState(emptyList()))


    fun setExplores(list: List<ExploreItem>) {
        findUsers.update {
            list
        }
    }
}

data class ExploreItem(
    val title: String,
    val findUsersQuery: FindUsersQuery,
)
data class ExploreItemState(
    val title: String,
    val findUsersQuery: FindUsersQuery,
    val loadingState: ResultState<List<User.Detail>>
)

data class ExploreUiState(
    val states: List<ExploreItemState>,
)