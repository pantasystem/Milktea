package jp.panta.misskeyandroidclient.viewmodel.antenna

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.list.UserList
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaToAdd
import jp.panta.misskeyandroidclient.model.antenna.Antenna
import jp.panta.misskeyandroidclient.model.group.Group
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.lang.StringBuilder
import java.util.regex.Pattern


@Suppress("BlockingMethodInNonBlockingContext")
class AntennaEditorViewModel (
    val antennaId: Antenna.Id?,
    val miCore: MiCore,
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore, val antennaId: Antenna.Id) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AntennaEditorViewModel(antennaId, miCore) as T
        }
    }

    val antenna = MutableLiveData<Antenna?>()


    private val mAntenna = MutableStateFlow<Antenna?>(null)

    enum class Source(val remote: String){
        HOME("home"), ALL("all"), USERS("users"), LIST("list"), GROUP("group")
    }

    val name = MediatorLiveData<String>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.name?: ""
        }
    }
    val source = MediatorLiveData<Source>().apply{
        addSource(this@AntennaEditorViewModel.antenna){ a ->
            Log.d("AntennaEditorVM", "antenna:$a")
            this.value = Source.values().firstOrNull {
                a?.src ==  it.remote
            }?: Source.ALL
        }
    }
    val keywords = MediatorLiveData<String>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = setupKeywords(it?.keywords)
        }
    }

    val excludeKeywords = MediatorLiveData<String>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = setupKeywords(it?.excludeKeywords)
        }
    }

    val userNames = MutableStateFlow<List<String>>(emptyList()).apply {
        mAntenna.filterNotNull().onEach {
            value = it.users
        }
    }

    private val mUsers = userNames.map { list ->
        list.map { userName ->
            val userNameAndHost = userName.split("@")
            UserViewData(userNameAndHost.first(), userNameAndHost.lastOrNull(), getAccount().accountId ,miCore, viewModelScope, Dispatchers.IO)

        }
    }

    val users = MutableStateFlow<List<UserViewData>>(emptyList()).apply {
        mUsers.onEach { list ->
            value = list
        }.launchIn(viewModelScope + Dispatchers.IO)
    }
    /*val users = MediatorLiveData<List<UserViewData>>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.users?.filter{ str ->
                str.isNotBlank()
            }?.map{ userId ->
                UserViewData(userId)
            }?: emptyList()
        }

        addSource(this){
            val i = account.getI(miCore.getEncryption())?: return@addSource
            it.forEach { uvd ->
                uvd.setApi(i, miCore.getMisskeyAPI(account))
            }
        }
    }*/



    val userListList = MediatorLiveData<List<UserList>>().apply{
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = getAccount()
                miCore.getMisskeyAPI(account).userList(I(account.getI(miCore.getEncryption()))).execute()?.body()
            }.onSuccess {
                postValue(it)
            }
        }
    }


    val userList = MediatorLiveData<UserList?>().apply{
        addSource(userListList){ list ->
            this.value = list.firstOrNull {
                it.id == this@AntennaEditorViewModel.antenna.value?.userListId
            }?: list.firstOrNull()
        }
    }

    val groupList = MediatorLiveData<List<Group>?>().apply{
        addSource(this@AntennaEditorViewModel.source){
            /*if(it == Source.GROUP && this.value.isNullOrEmpty()){
                miCore.getMisskeyAPI(accountRelation)
            }*/
        }
    }

    val group = MediatorLiveData<Group>().apply{
        addSource(groupList){
            this.value = it?.firstOrNull { g ->
                g.id == this@AntennaEditorViewModel.antenna.value?.userGroupId
            }?: it?.firstOrNull()
        }
    }

    /**
     * 新しいノートを通知します
     */
    val notify = MediatorLiveData<Boolean?>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.notify?: false
        }
    }

    /**
     * ファイルが添付されたノートのみ
     */
    val withFile =  MediatorLiveData<Boolean?>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.withFile?: false
        }
    }

    /**
     * 大文字と小文字を区別します
     */
    val caseSensitive =  MediatorLiveData<Boolean?>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.caseSensitive?: false
        }
    }

    /**
     * 返信を含める
     */
    val withReplies = MediatorLiveData<Boolean>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.withReplies?: false
        }
    }

    val antennaAddedStateEvent = EventBus<Boolean>()
    
    fun createOrUpdate(){

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = getAccount()
                val api = miCore.getMisskeyAPI(account) as MisskeyAPIV12
                val antenna = mAntenna.value
                val request = AntennaToAdd(
                    account.getI(miCore.getEncryption()),
                    antennaId?.antennaId,
                    name.value?: antenna?.name?: "",
                    source.value?.remote!!,
                    userList.value?.id,
                    null,
                    toListKeywords(keywords.value?: ""),
                    toListKeywords(excludeKeywords.value?: ""),
                    userNames.value,
                    caseSensitive.value?: false,
                    withFile.value?: false,
                    withReplies.value?: false,
                    notify.value?: false

                )
                val res = if(antennaId == null) {
                    api.createAntenna(request).execute()
                }else{
                    api.updateAntenna(request).execute()
                }

                res.body()?.toEntity(account)
            }.onSuccess {
                antennaAddedStateEvent.event = it != null
                mAntenna.value = it
            }.onFailure {
                antennaAddedStateEvent.event = false
            }


        }

    }

    val antennaRemovedEvent = EventBus<Unit>()

    fun removeRemote(){
        val antennaId = antennaId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = getAccount()
                (miCore.getMisskeyAPI(account) as MisskeyAPIV12).deleteAntenna(
                    AntennaQuery(antennaId = antennaId.antennaId, i = account.getI(miCore.getEncryption()), limit = null)
                ).execute()
            }.onSuccess {
                if(it.isSuccessful) {
                    antennaRemovedEvent.event = Unit
                }
            }
        }
    }
    val selectUserEvent = EventBus<List<User.Id>>()
    fun selectUser(){
        selectUserEvent.event = users.value.mapNotNull {
            it.user.value?.id
        }
    }

    
    private fun setupKeywords(keywords: List<List<String>>?): String{
        val builder = StringBuilder()
        keywords?.forEach {  list ->
            list.forEach {  word ->
                builder.append(word)
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    private fun toListKeywords(keywords: String): List<List<String>>{
        return keywords.split('\n').map{
            it.split(Pattern.compile("""[ 　]""")).filter{ str ->
                str.isNotEmpty()

            }
        }.filter{ list ->
            list.isNotEmpty()
        }
    }

    fun setUserNames(userNames: List<String>){
        this.userNames.value = userNames
    }

    private var mAccount: Account? = null
    private suspend fun getAccount(): Account {
        if(mAccount == null) {
            mAccount = antennaId?.let{
                miCore.getAccountRepository().get(it.accountId)
            }?: miCore.getAccountRepository().getCurrentAccount()
        }
        require(mAccount != null)
        return mAccount!!
    }
}