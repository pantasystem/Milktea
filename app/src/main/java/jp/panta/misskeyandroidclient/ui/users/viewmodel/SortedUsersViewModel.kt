package jp.panta.misskeyandroidclient.ui.users.viewmodel

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
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import java.io.Serializable

@OptIn(ExperimentalCoroutinesApi::class)
class SortedUsersViewModel @AssistedInject constructor(
    private val noteDataSourceAdder: NoteDataSourceAdder,
    loggerFactory: Logger.Factory,
    private val userDataSource: UserDataSource,
    private val accountStore: AccountStore,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val encryption: Encryption,
    @Assisted type: Type?,
    @Assisted conditions: UserRequestConditions?
) : ViewModel() {

    @AssistedFactory
    interface AssistedViewModelFactory {
        fun create(type: Type?, conditions: UserRequestConditions?): SortedUsersViewModel
    }

    companion object;

    private val orderBy: UserRequestConditions = type?.conditions ?: conditions!!

    val logger = loggerFactory.create("SortedUsersViewModel")


    data class UserRequestConditions(
        val origin: RequestUser.Origin?,
        val sort: String?,
        val state: RequestUser.State?
    ) : Serializable {
        fun toRequestUser(i: String): RequestUser {
            return RequestUser(
                i = i,
                origin = origin?.origin,
                sort = sort,
                state = state?.state
            )
        }
    }

    enum class Type(val conditions: UserRequestConditions) {
        TRENDING_USER(
            UserRequestConditions(
                origin = RequestUser.Origin.LOCAL,
                sort = RequestUser.Sort().follower().asc(),
                state = RequestUser.State.ALIVE
            )
        ),
        USERS_WITH_RECENT_ACTIVITY(
            UserRequestConditions(
                origin = RequestUser.Origin.LOCAL,
                sort = RequestUser.Sort().updatedAt().asc(),
                state = null
            )
        ),
        NEWLY_JOINED_USERS(
            UserRequestConditions(
                origin = RequestUser.Origin.LOCAL,
                sort = RequestUser.Sort().createdAt().asc(),
                state = RequestUser.State.ALIVE
            )
        ),

        REMOTE_TRENDING_USER(
            UserRequestConditions(
                origin = RequestUser.Origin.REMOTE,
                sort = RequestUser.Sort().follower().asc(),
                state = RequestUser.State.ALIVE
            )
        ),
        REMOTE_USERS_WITH_RECENT_ACTIVITY(
            UserRequestConditions(
                origin = RequestUser.Origin.COMBINED,
                sort = RequestUser.Sort().updatedAt().asc(),
                state = RequestUser.State.ALIVE
            )
        ),
        NEWLY_DISCOVERED_USERS(
            UserRequestConditions(
                origin = RequestUser.Origin.COMBINED,
                sort = RequestUser.Sort().createdAt().asc(),
                state = null
            )
        ),

    }

    private val userIds = MutableStateFlow<List<User.Id>>(emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    val users = userDataSource.state.flatMapLatest { state ->
        userIds.map { list ->
            list.mapNotNull {
                state.get(it) as? User.Detail
            }
        }
    }.flowOn(Dispatchers.IO).asLiveData()


    val isRefreshing = MutableLiveData<Boolean>()

    fun loadUsers() {

        val account = accountStore.currentAccount
        val i = account?.getI(encryption)

        if (i == null) {
            isRefreshing.value = false
            return
        } else {
            isRefreshing.value = true
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                misskeyAPIProvider.get(account).getUsers(orderBy.toRequestUser(i))
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
    type: SortedUsersViewModel.Type?,
    orderBy: SortedUsersViewModel.UserRequestConditions?

) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(type, orderBy) as T
    }
}