package net.pantasystem.milktea.antenna.viewmodel

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.antenna.*
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListRepository
import net.pantasystem.milktea.model.user.User
import java.util.regex.Pattern
import javax.inject.Inject



@Suppress("BlockingMethodInNonBlockingContext")
@HiltViewModel
class AntennaEditorViewModel @Inject constructor(
    private val userViewDataFactory: UserViewData.Factory,
    loggerFactory: Logger.Factory,
    private val accountRepository: AccountRepository,
    private val userListRepository: UserListRepository,
    private val antennaRepository: AntennaRepository,
) : ViewModel(){


    private val logger = loggerFactory.create("AntennaEditorViewModel")


    private val _antennaId = MutableStateFlow<Antenna.Id?>(null)


    private val mAntenna = MutableStateFlow<Antenna?>(null).apply {
        viewModelScope.launch {
            runCancellableCatching {
                fetch()
            }.onFailure {
                logger.debug("アンテナ取得エラー", e = it)
            }.onSuccess {
                logger.debug("取得成功:$it")
                value = it
            }
        }
    }

    val antenna = MutableLiveData<Antenna?>().apply {
        mAntenna.filterNotNull().onEach {
            postValue(it)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }


    val name = MediatorLiveData<String>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.name?: ""
        }
    }
    val source = MediatorLiveData<AntennaSource>().apply{
        addSource(this@AntennaEditorViewModel.antenna){ a ->
            Log.d("AntennaEditorVM", "antenna:$a")
            this.value = AntennaSource.values().firstOrNull {
                a?.src ==  it
            }?: AntennaSource.All
        }
    }



    val isList = Transformations.map(source) {
        it is AntennaSource.List
    }

    val isUsers = Transformations.map(source) {
        it is AntennaSource.Users
    }

    val isGroup = Transformations.map(source) {
        it is AntennaSource.Users
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

    private val userNames = MutableStateFlow<List<String>>(emptyList()).apply {
        mAntenna.filterNotNull().onEach {
            value = it.users
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    private val mUsers = userNames.map { list ->
        list.map { userName: String ->
            val userNameAndHost = userName.split("@").filterNot { it == "" }
            userViewDataFactory.create(userNameAndHost[0], userNameAndHost.getOrNull(1), getAccount().getOrThrow().accountId, viewModelScope, Dispatchers.IO)

        }
    }

    @ExperimentalCoroutinesApi
    val users = MutableStateFlow<List<UserViewData>>(emptyList()).apply {
        mUsers.onEach { list ->
            value = list
        }.launchIn(viewModelScope + Dispatchers.IO)
    }


    val userListList = MediatorLiveData<List<UserList>>().apply{
        viewModelScope.launch {
            runCancellableCatching {
                val account = getAccount().getOrThrow()
                userListRepository.findByAccountId(account.accountId)
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
        addSource(antenna) { antenna ->
            this.value = this@AntennaEditorViewModel.userListList.value?.firstOrNull {
                it.id == antenna?.userListId
            }
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

        viewModelScope.launch {
            runCancellableCatching {
                val account = getAccount().getOrThrow()
                val antenna = mAntenna.value
                val params = SaveAntennaParam(
                    name.value ?: antenna?.name ?: "",
                    source.value ?: AntennaSource.All,
                    userList.value?.id,
                    null,
                    toListKeywords(keywords.value ?: ""),
                    toListKeywords(excludeKeywords.value ?: ""),
                    userNames.value,
                    caseSensitive.value ?: false,
                    withFile.value ?: false,
                    withReplies.value ?: false,
                    notify.value ?: false,
                    )
                if (antenna == null) {
                    antennaRepository.create(account.accountId, params)
                        .getOrThrow()
                } else {
                    antennaRepository.update(antenna.id, params)
                        .getOrThrow()
                }
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    antennaAddedStateEvent.event = true
                    mAntenna.value = it
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    antennaAddedStateEvent.event = false
                }
            }


        }

    }

    val antennaRemovedEvent = EventBus<Unit>()

    fun removeRemote(){
        val antennaId = _antennaId.value ?: return
        viewModelScope.launch {
            antennaRepository.delete(antennaId).onSuccess {
                withContext(Dispatchers.Main) {
                    antennaRemovedEvent.event = Unit
                }
            }
        }
    }
    val selectUserEvent = EventBus<List<User.Id>>()
    @ExperimentalCoroutinesApi
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
        logger.debug("setUserNames: $userNames")
        this.userNames.value = userNames
    }

    fun setAntennaId(antennaId: Antenna.Id) {
        _antennaId.value = antennaId
    }

    private suspend fun fetch(): Antenna? {
        return _antennaId.value?.let{ antennaId ->
            antennaRepository.find(antennaId).getOrNull()
        }
    }

    private suspend fun getAccount(): Result<Account> = runCancellableCatching {
        _antennaId.value?.let {
            accountRepository.get(it.accountId).getOrThrow()
        } ?: accountRepository.getCurrentAccount().getOrThrow()
    }
}