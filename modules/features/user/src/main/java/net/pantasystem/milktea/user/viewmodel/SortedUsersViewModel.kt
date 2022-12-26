package net.pantasystem.milktea.user.viewmodel

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.api.misskey.users.from
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.query.FindUsersQuery

@OptIn(ExperimentalCoroutinesApi::class)
class SortedUsersViewModel @AssistedInject constructor(
    private val noteDataSourceAdder: NoteDataSourceAdder,
    loggerFactory: Logger.Factory,
    private val userDataSource: UserDataSource,
    private val accountStore: AccountStore,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    @Assisted val findUsersQuery: FindUsersQuery,
) : ViewModel() {

    @AssistedFactory
    interface AssistedViewModelFactory {
        fun create(findUsersQuery: FindUsersQuery): SortedUsersViewModel
    }

    companion object


    val logger = loggerFactory.create("SortedUsersViewModel")


    private val userIds = MutableStateFlow<List<User.Id>>(emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    val users = userIds.flatMapLatest { list ->
        userDataSource.observeIn(accountStore.currentAccountId!!, list.map { it.id }).map { users ->
            users.mapNotNull {
                it as? User.Detail
            }

        }
    }.flowOn(Dispatchers.IO).asLiveData()


    val isRefreshing = MutableLiveData<Boolean>()

    fun loadUsers() {

        val account = accountStore.currentAccount
        val i = account?.token

        if (i == null) {
            isRefreshing.value = false
            return
        } else {
            isRefreshing.value = true
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCancellableCatching {
                misskeyAPIProvider.get(account).getUsers(RequestUser.from(findUsersQuery, i))
                    .body()
            }
                .map {
                    it?.map { dto ->
                        dto.pinnedNotes?.map { noteDTO ->
                            noteDataSourceAdder.addNoteDtoToDataSource(account, noteDTO)
                        }
                        dto.toUser(account, true).also { u ->
                            userDataSource.add(u)
                        }
                    }?.map { u ->
                        u.id
                    } ?: emptyList()
                }.onFailure { t ->
                    logger.error("ユーザーを取得しようとしたところエラーが発生しました", t)
                }.onSuccess {
                    userIds.value = it
                }
            isRefreshing.postValue(false)
        }


    }


}

@Suppress("UNCHECKED_CAST")
fun SortedUsersViewModel.Companion.providerViewModel(
    factory: SortedUsersViewModel.AssistedViewModelFactory,
    findUsersQuery: FindUsersQuery?,
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(findUsersQuery ?: FindUsersQuery(null, null, null)) as T
    }
}