package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.account.Account
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

@ExperimentalCoroutinesApi
class UserDetailViewModel(
    val userId: User.Id?,
    val fqdnUserName: String?,
    val miCore: MiCore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val encryption: Encryption = miCore.getEncryption()
) : ViewModel(){
    val tag=  "userDetailViewModel"

    val user = MutableLiveData<User.Detail>()
    private val userState = MutableStateFlow<User.Detail?>(null).apply {
        onEach {
            user.postValue(it)
        }.launchIn(viewModelScope)
    }


    private val pinNotesState = userState.filterNotNull().map {
        it.pinnedNoteIds?.map { id ->
            miCore.getNoteDataSource().get(id)
        }?: emptyList()
    }

    val pinNotes = MediatorLiveData<List<PlaneNoteViewData>>().apply {

        pinNotesState.map{ notes ->
            notes.map { note ->
                PlaneNoteViewData(miCore.getGetters().noteRelationGetter.get(note), getAccount(), DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter())
            }
        }.onEach {
            this.postValue(it)

        }.flatMapLatest {
            it.map {  n ->
                n.eventFlow
            }.merge()
        }.launchIn(viewModelScope + dispatcher)
    }

    val isMine = MutableLiveData<Boolean>()
    private val isMeState = userState.filterNotNull().map {
        miCore.getAccountRepository().get(it.id.accountId).remoteId == it.id.id
    }.onEach {
        isMine.postValue(it)
    }.launchIn(viewModelScope + Dispatchers.IO)

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
        viewModelScope.launch(dispatcher) {
            val userDTO = fetchUserDTO()
                ?: return@launch
            val u = userDTO.toUser(getAccount(), true)
            miCore.getUserDataSource().add(u)

            userDTO.pinnedNotes?.map { noteDTO ->
                noteDTO.toEntities(getAccount())
            }?.forEach { entities ->
                miCore.getUserDataSource().addAll(entities.third)
                miCore.getNoteDataSource().addAll(entities.second)
            }
            userState.value = u as? User.Detail

        }

    }

    private suspend fun fetchUserDTO(): UserDTO? {
        val userNameList = fqdnUserName?.split("@")?.filter{ it.isNotBlank() }
        Log.d(tag, "userNameList:$userNameList, fqcnUserName:$fqdnUserName")
        val userName = userNameList?.firstOrNull()
        val host = runCatching { userNameList?.get(1) }.getOrNull()
        val account = getAccount()
        val req = RequestUser(
            i = getAccount().getI(encryption),
            userId = userId?.id,
            userName = userName,
            host = host,
            detail = true
        )
        return runCatching {
            miCore.getMisskeyAPI(account).showUser(req).execute()?.body()
        }.getOrNull()

    }

    fun changeFollow(){
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let{
                runCatching {
                    val user = miCore.getUserRepository().find(it.id) as User.Detail
                    if(user.isFollowing) {
                        miCore.getUserRepository().unfollow(user.id)
                    }else{
                        miCore.getUserRepository().follow(user.id)
                    }
                    miCore.getUserRepository().find(user.id) as User.Detail
                }.onSuccess {
                    userState.value = it
                }
            }

        }
    }

    fun showFollows(){
        showFollows.event = user.value
    }

    fun showFollowers(){
        showFollowers.event = user.value
    }

    fun mute(){
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let{
                runCatching {
                    miCore.getUserRepository().mute(it.id)
                    miCore.getUserRepository().find(it.id, true) as User.Detail
                }.onSuccess {
                    userState.value = it
                }
            }
        }
    }

    fun unmute(){
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let{
                runCatching {
                    miCore.getUserRepository().unmute(it.id)
                    miCore.getUserRepository().find(it.id, true) as User.Detail
                }.onSuccess {
                    userState.value = it
                }
            }
        }
    }

    fun block(){
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let{
                runCatching {
                    miCore.getUserRepository().block(it.id)
                    (miCore.getUserRepository().find(it.id, true) as User.Detail)
                }.onSuccess {
                    userState.value = it
                }
            }
        }
    }

    fun unblock(){
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let{
                runCatching {
                    miCore.getUserRepository().unblock(it.id)
                    (miCore.getUserRepository().find(it.id, true) as User.Detail)
                }.onSuccess {
                    userState.value = it
                }
            }
        }
    }



    private suspend fun createUserIdOnlyRequest(): RequestUser {
        return RequestUser(i = getAccount().getI(encryption), userId = userId?.id)
    }


    var mAc: Account? = null
    private suspend fun getAccount(): Account {
        if(mAc != null) {
            return mAc!!
        }
        if(userId != null) {
            mAc = miCore.getAccountRepository().get(userId.accountId)
            return mAc!!
        }

        mAc = miCore.getAccountRepository().getCurrentAccount()
        return mAc!!
    }

}