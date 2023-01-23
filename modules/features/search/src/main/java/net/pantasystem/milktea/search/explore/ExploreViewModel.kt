package net.pantasystem.milktea.search.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
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
    val userRepository: UserRepository,
    loggerFactory: Logger.Factory,
) : ViewModel() {
    val logger = loggerFactory.create("ExploreViewModel")

    private val findUsers = MutableStateFlow<List<ExploreItem>>(emptyList())

    private val rawLoadingStates =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
            findUsers.map { list ->
                list.map {
                    it to suspend {
                        it to userRepository.findUsers(ac.accountId, it.findUsersQuery)
                    }.asLoadingStateFlow()
                }
            }
        }.flatMapLatest { exploreItems ->
            combine(exploreItems.map { it.second }) { states ->
                states.toList()
            }.map { list ->
                list.mapIndexed { index, resultState ->
                    val exploreItem = exploreItems[index].first
                    ExploreResultState(
                        findUsersQuery = exploreItem.findUsersQuery,
                        loadingState = resultState.suspendConvert {
                            it.second.map { user ->
                                user.id
                            }
                        },
                        title = exploreItem.title,
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState = rawLoadingStates.map { list ->
        list.map { state ->
            val rawContent = (state.loadingState.content as? StateContent.Exist)
                ?.rawContent ?: emptyList()
            state to userDataSource.observeIn(accountStore.currentAccountId!!, rawContent.map {
                it.id
            }).mapNotNull { users ->
                users.mapNotNull {
                    it as? User.Detail?
                }
            }
        }
    }.map { statePair ->
        val flows = statePair.map { pair ->
            pair.second
        }
        combine(flows) { users ->
            users.mapIndexed { index, list ->
                val explore = statePair[index].first
                ExploreItemState(
                    title = explore.title,
                    loadingState = explore.loadingState.convert {
                        list
                    },
                    findUsersQuery = explore.findUsersQuery
                )
            }
        }
    }.flatMapLatest { flow ->
        flow.map {
            ExploreUiState(it)
        }
    }.catch { e ->
        logger.error("get explores error", e)
    }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, ExploreUiState(emptyList()))


    val account = accountStore.observeCurrentAccount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

data class ExploreResultState(
    val title: String,
    val findUsersQuery: FindUsersQuery,
    val loadingState: ResultState<List<User.Id>>
)

data class ExploreUiState(
    val states: List<ExploreItemState>,
)