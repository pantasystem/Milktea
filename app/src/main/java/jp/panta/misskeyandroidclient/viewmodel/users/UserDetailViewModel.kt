package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IndexOutOfBoundsException

class UserDetailViewModel(
    val account: Account,
    val misskeyAPI: MisskeyAPI,
    val userId: String?,
    val fqcnUserName: String?,
    val encryption: Encryption,
    val miCore: MiCore
) : ViewModel(){
    val tag=  "userDetailViewModel"

    val user = MutableLiveData<UserDTO>()
    val isMine = account.remoteId == userId

    val pinNotes = MediatorLiveData<List<PlaneNoteViewData>>().apply{
        addSource(user){
            this.value = it.pinnedNotes?.map{note ->
                PlaneNoteViewData(note, account, DetermineTextLengthSettingStore(miCore.getSettingStore()))
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
            if(it == true){
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

    val isBlocking = MediatorLiveData<Boolean>().apply{
        value = user.value?.isBlocking?: false
        addSource(user){
            value = it.isBlocking
        }
    }

    val isMuted = MediatorLiveData<Boolean>().apply{
        value = user.value?.isMuted?: false
        addSource(user){
            value = it.isMuted
        }
    }

    val isRemoteUser = MediatorLiveData<Boolean>().apply{
        addSource(user){
            value = it.url != null
        }
    }
    val showFollowers = EventBus<UserDTO?>()
    val showFollows = EventBus<UserDTO?>()

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
                i = account.getI(encryption),
                userId = userId,
                userName = userName,
                host = host
            )
        ).enqueue(object : Callback<UserDTO>{
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                val user = response.body()
                if(user != null){
                    this@UserDetailViewModel.user.postValue(user)
                }else{
                    Log.d(tag, "ユーザーの読み込みに失敗しました, userId:$userId, userName: $userName, host: $host")
                }
            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                Log.e(tag, "ユーザーの読み込みに失敗しました", t)
            }
        })
    }

    fun changeFollow(){
        val isFollowing = isFollowing.value?: false
        if(isFollowing){
            misskeyAPI.unFollowUser(RequestUser(account.getI(encryption), userId = userId)).enqueue(
                object : Callback<UserDTO>{
                    override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                        if(response.code() == 200){
                            this@UserDetailViewModel.isFollowing.postValue(false)
                        }
                    }

                    override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    }
                }
            )
        }else{
            misskeyAPI.followUser(RequestUser(account.getI(encryption), userId = userId)).enqueue(object : Callback<UserDTO>{
                override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                    if(response.code() == 200){
                        this@UserDetailViewModel.isFollowing.postValue(true)
                    }
                }

                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
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

    fun mute(){
        misskeyAPI::muteUser.postUserIdAndStateChange(isMuted, true, 204)
    }

    fun unmute(){
        misskeyAPI::muteUser.postUserIdAndStateChange(isMuted, false, 204)
    }

    fun block(){
        misskeyAPI::blockUser.postUserIdAndStateChange(isBlocking, true, 200)
    }

    fun unblock(){
        misskeyAPI::unblockUser.postUserIdAndStateChange(isBlocking, false, 200)
    }


    private fun ((RequestUser)->Call<Unit>).postUserIdAndStateChange(liveData: MediatorLiveData<Boolean>, valueOnSuccess: Boolean, codeOnSuccess: Int){
        this(createUserIdOnlyRequest()).enqueue(object :Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() == codeOnSuccess){
                    liveData.postValue(valueOnSuccess)
                }else{
                    Log.d(tag, "失敗しました, code:${response.code()}")
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
            }
        })
    }

    private fun createUserIdOnlyRequest(): RequestUser {
        return RequestUser(i = account.getI(encryption), userId = userId)
    }

}