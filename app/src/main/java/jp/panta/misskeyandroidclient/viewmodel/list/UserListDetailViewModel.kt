package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.list.ListId
import jp.panta.misskeyandroidclient.api.list.ListUserOperation
import jp.panta.misskeyandroidclient.api.list.UpdateList
import jp.panta.misskeyandroidclient.api.list.UserListDTO
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentLinkedDeque
@FlowPreview
@ExperimentalCoroutinesApi
class UserListDetailViewModel(
    val listId: UserList.Id,
    val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val listId: UserList.Id, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UserListDetailViewModel(listId, miCore) as T
        }
    }

    private val tag = this.javaClass.simpleName





    private val mUserMap = LinkedHashMap<User.Id, UserViewData>()

    //private val mPublisher = UserListEventStore(misskeyAPI, account).getEventStream()
    val updateEvents = ConcurrentLinkedDeque<UserListEvent>()


    private val mUserList = MutableLiveData<UserList>()

    private val mListUsers = MutableLiveData<List<UserViewData>>()

    val userList: LiveData<UserList> = mUserList


    val listUsers: LiveData<List<UserViewData>> = mListUsers

    private val logger = miCore.loggerFactory.create("UserListDetailViewModel")

    init{


        load()

        mUserList.observeForever { ul ->
            loadUsers(ul.userIds)

        }
    }

    fun load(){
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = getAccount()
                val res = miCore.getMisskeyAPI(account).showList(
                    ListId(
                        i = account.getI(miCore.getEncryption()),
                        listId = listId.userListId
                    )
                )
                res.throwIfHasError()
                res.body()?.toEntity(account)
            }.onSuccess {
                mUserList.postValue(it)
            }.onFailure {
                logger.error("load list error", e = it)
            }

        }

    }

    fun updateName(name: String){
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = getAccount()
                val res = miCore.getMisskeyAPI(account).updateList(
                    UpdateList(
                        i = account.getI(miCore.getEncryption()),
                        listId = listId.userListId,
                        name = name
                    )
                )
                res.throwIfHasError()

            }.onSuccess {
                updateEvents.add(
                    UserListEvent(userListId = listId, type = UserListEvent.Type.UPDATED_NAME)
                )
                load()
            }.onFailure { t ->
                logger.error("名前の更新に失敗した", e = t)
            }
        }

    }

    fun pushUser(userId: User.Id){

        viewModelScope.launch(Dispatchers.IO){
            runCatching {
                val account = getAccount()
                val res = miCore.getMisskeyAPI(account).pushUserToList(
                    ListUserOperation(
                        i = account.getI(miCore.getEncryption()),
                        listId = listId.userListId,
                        userId = userId.id
                    )
                )
                res.throwIfHasError()

            }.onSuccess {
                onPushedUser(userId)

            }
        }

    }


    fun pullUser(userId: User.Id){

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = miCore.getAccountRepository().getCurrentAccount()
                val result = miCore.getMisskeyAPI(account).pullUserFromList(
                    ListUserOperation(
                        i = account.getI(miCore.getEncryption()),
                        listId = listId.userListId,
                        userId = userId.id
                    )
                )
                result.throwIfHasError()
                if(result.isSuccessful) userId else null
            }.onFailure { t ->
                Log.d(tag, "pull user error", t)
            }.onSuccess {
                onPulledUser(userId)
            }
        }


    }
    private fun loadUsers(userIds: List<User.Id>){

        Log.d(tag, "load users $userIds")
        mUserMap.clear()

        val listUserViewDataList = userIds.map{ userId ->
            UserViewData(userId, miCore, viewModelScope)
        }

        val list = listUserViewDataList.mapNotNull {
            (it.userId ?: it.user.value?.id)?.let { id ->
                id to it
            }
        }.toMap()
        mUserMap.putAll(
            list
        )
        mListUsers.postValue(mUserMap.values.toList())
    }

    private fun onPushedUser(userId: User.Id){
        val newUser = UserViewData(userId, miCore, viewModelScope, Dispatchers.IO)
        mUserMap[userId] = newUser
        adaptUsers()

        updateEvents.add(UserListEvent(
            userListId = listId,
            userId = userId,
            type = UserListEvent.Type.PUSH_USER
        ))
    }

    private fun onPulledUser(userId: User.Id){
        mUserMap.remove(userId)
        adaptUsers()

        updateEvents.add(UserListEvent(
            userListId = listId,
            userId = userId,
            type = UserListEvent.Type.PULL_USER
        ))
    }




    private fun adaptUsers(){
        mListUsers.postValue(mUserMap.values.toList())
    }

    private suspend fun getAccount(): Account {
        return miCore.getAccountRepository().get(listId.accountId)
    }

}