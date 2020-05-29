package jp.panta.misskeyandroidclient.viewmodel.users.explore

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class ExploreUserViewModel(
    val miCore: MiCore,
    val type: Type
) : ViewModel(){

    data class UserRequestConditions(
        val origin: RequestUser.Origin?,
        val sort: String?,
        val state: RequestUser.State?
    ): Serializable{
        fun toRequestUser(i: String): RequestUser{
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

    private val listener = Listener()

    val users = object : MediatorLiveData<List<UserViewData>>(){
        override fun onActive() {
            super.onActive()
            miCore.currentAccount.value?.let{ ar ->
                miCore.getMainCapture(ar).putListener(listener)
            }

        }

        override fun onInactive() {
            super.onInactive()

            miCore.currentAccount.value?.let{ ar ->
                miCore.getMainCapture(ar).removeListener(listener)
            }
        }
    }.apply{
        addSource(miCore.currentAccount){
            if(this.value.isNullOrEmpty()){
                loadUsers()
            }
        }
    }

    fun loadUsers(){
        val ci = miCore.currentAccount.value?.getCurrentConnectionInformation()
        val i = ci?.getI(miCore.getEncryption())
            ?: return
        miCore.getMisskeyAPI(ci).getUsers(type.conditions.toRequestUser(i)).enqueue(object : Callback<List<User>>{
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if(response.code() in 200 until 300){
                    users.postValue(response.body()?.map{
                        UserViewData(it)
                    })
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {

            }
        })

    }

    inner class Listener : MainCapture.AbsListener(){
        override fun follow(user: User) {
            super.follow(user)
            updateUser(user)
        }

        override fun unFollowed(user: User) {
            super.unFollowed(user)
            updateUser(user)
        }
        override fun followed(user: User) {
            super.followed(user)
            updateUser(user)
        }

        private fun updateUser(user: User){
            val list = ArrayList(users.value?: emptyList())
            list.forEach {
                if(it.userId == user.id){
                    it.user.postValue(user)
                }
            }
        }

    }
}