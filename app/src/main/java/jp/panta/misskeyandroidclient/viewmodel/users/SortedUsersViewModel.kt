package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
class SortedUsersViewModel(
    val miCore: MiCore,
    type: Type?,
    orderBy: UserRequestConditions?
) : ViewModel(){
    val orderBy: UserRequestConditions = type?.conditions?: orderBy!!

    val logger = miCore.loggerFactory.create("SortedUsersViewModel")

    class Factory(val miCore: MiCore, val type: Type?, private val orderBy: UserRequestConditions?) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SortedUsersViewModel(
                miCore,
                type,
                orderBy
            ) as T
        }
    }

    data class UserRequestConditions(
        val origin: RequestUser.Origin?,
        val sort: String?,
        val state: RequestUser.State?
    ): Serializable{
        fun toRequestUser(i: String): RequestUser {
            return RequestUser(
                i = i,
                origin = origin?.origin,
                sort = sort,
                state = state?.state
            )
        }
    }

    enum class Type(val conditions: UserRequestConditions){
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


    val users = object : MediatorLiveData<List<UserViewData>>(){

    }.apply{
        miCore.getCurrentAccount().onEach {
            loadUsers()
        }.launchIn(viewModelScope + Dispatchers.Main)
    }

    val isRefreshing = MutableLiveData<Boolean>()

    fun loadUsers(){

        val account = miCore.getCurrentAccount().value
        val i = account?.getI(miCore.getEncryption())

        if(i == null){
            isRefreshing.value = false
            return
        }else{
            isRefreshing.value = true
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { miCore.getMisskeyAPI(account).getUsers(orderBy.toRequestUser(i)).execute().body() }
                .map {
                    it?.map{ dto ->
                        dto.toUser(account).also{ u ->
                            miCore.getUserDataSource().add(u)
                        }
                    }?.map{ u->
                        UserViewData(u, miCore, viewModelScope, Dispatchers.IO)
                    }?: emptyList()
                }.onFailure { t ->
                    logger.error("ユーザーを取得しようとしたところエラーが発生しました", t)
                }.onSuccess {
                    users.postValue(it)
                }
            isRefreshing.postValue(false)
        }


    }


}