package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.gettters.NoteRelationGetter
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IndexOutOfBoundsException

@ExperimentalCoroutinesApi
class UserDetailViewModel(
    val account: Account,
    val misskeyAPI: MisskeyAPI,
    val userId: String?,
    val fqcnUserName: String?,
    val encryption: Encryption,
    val miCore: MiCore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(){
    val tag=  "userDetailViewModel"

    val user = MutableLiveData<User.Detail>()
    private val userState = MutableStateFlow<User.Detail?>(null).apply {
        onEach {
            user.postValue(it)
        }.launchIn(viewModelScope)
    }

    val isMine = account.remoteId == userId

    private val pinNotesState = userState.filterNotNull().map {
        it.pinnedNoteIds?.map { id ->
            miCore.getNoteRepository().get(id)
        }?: emptyList()
    }

    val pinNotes = MediatorLiveData<List<PlaneNoteViewData>>().apply {

        pinNotesState.map{ notes ->
            notes.map { note ->
                PlaneNoteViewData(miCore.getGetters().noteRelationGetter.get(note), account, DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter())
            }
        }.onEach {
            this.postValue(it)

        }.flatMapLatest {
            it.map {  n ->
                n.eventFlow
            }.merge()
        }.launchIn(viewModelScope)
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
        value = user.value?.isMuting?: false
        addSource(user){
            value = it.isMuting
        }
    }

    val isRemoteUser = MediatorLiveData<Boolean>().apply{
        addSource(user){
            value = it.url != null
        }
    }
    val showFollowers = EventBus<User?>()
    val showFollows = EventBus<User?>()

    fun load(){
        viewModelScope.launch() {
            val userDTO = fetchUserDTO()
                ?: return@launch
            val u = userDTO.toUser(account, true)
            miCore.getUserRepository().add(u)

            userDTO.pinnedNotes?.map { noteDTO ->
                noteDTO.toEntities(account)
            }?.forEach { entities ->
                miCore.getUserRepository().addAll(entities.third)
                miCore.getNoteRepository().addAll(entities.second)
            }
            userState.value = u as? User.Detail

        }

    }

    private fun fetchUserDTO(): UserDTO? {
        val userNameList = fqcnUserName?.split("@")?.filter{ it.isNotBlank() }
        Log.d(tag, "userNameList:$userNameList, fqcnUserName:$fqcnUserName")
        val userName = userNameList?.firstOrNull()
        val host = runCatching { userNameList?.get(1) }.getOrNull()

        val req = RequestUser(
            i = account.getI(encryption),
            userId = userId,
            userName = userName,
            host = host,
            detail = true
        )
        return runCatching {
            misskeyAPI.showUser(req).execute()?.body()
        }.getOrNull()

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