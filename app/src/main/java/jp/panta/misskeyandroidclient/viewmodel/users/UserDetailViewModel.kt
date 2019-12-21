package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IndexOutOfBoundsException

class UserDetailViewModel(
    val connectionInstance: ConnectionInstance,
    val misskeyAPI: MisskeyAPI,
    val userId: String?,
    val fqcnUserName: String?
) : ViewModel(){
    val tag=  "userDetailViewModel"

    val user = MutableLiveData<User>()
    val isMine = connectionInstance.userId == userId

    val pinNotes = MediatorLiveData<List<PlaneNoteViewData>>().apply{
        addSource(user){
            this.value = it.pinnedNotes?.map{note ->
                PlaneNoteViewData(note, connectionInstance)
            }
        }
    }

    val isFollowing = MediatorLiveData<Boolean>().apply{
        addSource(user){
            this.value = it.isFollowing
        }
    }

    val followButtonStatus = MediatorLiveData<String>().apply{
        addSource(isFollowing){
            if(it){
                this.value = "フォロー中"
            }else{
                this.value = "フォロー"
            }
        }
    }

    val userName = MediatorLiveData<String>().apply{
        addSource(user){user ->
            this.value = "@" + user.userName + if(user.host != null){
                "@${user.host}"
            }else{
                ""
            }
        }
    }

    val showFollowers = EventBus<User?>()
    val showFollows = EventBus<User?>()

    fun load(){
        val userNameList = fqcnUserName?.split("@")?.filter{
            it.isNotBlank()
        }
        Log.d(tag, "userNameList:$userNameList, fqcnUserName:$fqcnUserName")
        val userName = userNameList?.firstOrNull()
        val host = try{
            userNameList?.get(1)
        }catch(e: IndexOutOfBoundsException){
            null
        }
        misskeyAPI.showUser(
            RequestUser(
                i = connectionInstance.getI()!!,
                userId = userId,
                userName = userName,
                host = host
            )
        ).enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()
                if(user != null){
                    this@UserDetailViewModel.user.postValue(user)
                }else{
                    Log.d(tag, "ユーザーの読み込みに失敗しました, userId:$userId, userName: $userName, host: $host")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(tag, "ユーザーの読み込みに失敗しました", t)
            }
        })
    }

    fun changeFollow(){
        val isFollowing = isFollowing.value?: false
        if(isFollowing){
            misskeyAPI.unFollowUser(RequestUser(connectionInstance.getI()!!, userId = userId)).enqueue(
                object : Callback<User>{
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if(response.code() == 200){
                            this@UserDetailViewModel.isFollowing.postValue(false)
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                    }
                }
            )
        }else{
            misskeyAPI.followUser(RequestUser(connectionInstance.getI()!!, userId = userId)).enqueue(object : Callback<User>{
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if(response.code() == 200){
                        this@UserDetailViewModel.isFollowing.postValue(true)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                }
            })
        }
    }

    fun showFollows(){
        showFollows.event = user.value
    }

    fun showFollowers(){
        showFollowers.event = user.value
    }

}