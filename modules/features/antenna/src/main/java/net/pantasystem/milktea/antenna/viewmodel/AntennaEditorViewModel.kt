package net.pantasystem.milktea.antenna.viewmodel

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


    private val mAntenna = MutableStateFlow<Antenna?>(null)

    val antenna = mAntenna.asLiveData()


    val name = MediatorLiveData<String>()

    val source = MediatorLiveData<AntennaSource>()


    val isList = Transformations.map(source) {
        it is AntennaSource.List
    }

    val isUsers = Transformations.map(source) {
        it is AntennaSource.Users
    }

    val isGroup = Transformations.map(source) {
        it is AntennaSource.Users
    }

    val keywords = MediatorLiveData<String>()

    val excludeKeywords = MediatorLiveData<String>()

    private val userNames = MutableStateFlow<List<String>>(emptyList())

    @ExperimentalCoroutinesApi
    private val mUsers = userNames.map { list ->
        list.filterNot {
            it.isBlank()
        }.map { userName: String ->
            val userNameAndHost = userName.split("@").filterNot { it == "" }
            userViewDataFactory.create(userNameAndHost[0], userNameAndHost.getOrNull(1), getAccount().getOrThrow().accountId, viewModelScope, Dispatchers.IO)
        }
    }

    @ExperimentalCoroutinesApi
    val users: StateFlow<List<UserViewData>> = mUsers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )


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
    val notify = MediatorLiveData<Boolean?>()

    /**
     * ファイルが添付されたノートのみ
     */
    val withFile =  MediatorLiveData<Boolean?>()

    /**
     * 大文字と小文字を区別します
     */
    val caseSensitive =  MediatorLiveData<Boolean?>()
    /**
     * 返信を含める
     */
    val withReplies = MediatorLiveData<Boolean>()

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
        logger.debug { "setUserNames: $userNames" }
        this.userNames.value = userNames
    }

    fun setAntennaId(antennaId: Antenna.Id) {
        _antennaId.value = antennaId
        viewModelScope.launch {
            antennaRepository.find(antennaId).onSuccess { antenna ->
                name.postValue(antenna.name)
                source.postValue(
                    AntennaSource.values().firstOrNull {
                        antenna.src ==  it
                    }?: AntennaSource.All
                )
                keywords.postValue(setupKeywords(antenna.keywords))
                excludeKeywords.postValue(setupKeywords(antenna.excludeKeywords))
                userNames.value = antenna.users
                notify.postValue(antenna.notify)
                withFile.postValue(antenna.withFile)
                caseSensitive.postValue(antenna.caseSensitive)
                withReplies.postValue(antenna.withReplies)
                mAntenna.value = antenna
            }.onFailure {
                logger.error("fetch antenna filed", it)
            }
        }
    }

    private suspend fun getAccount(): Result<Account> = runCancellableCatching {
        _antennaId.value?.let {
            accountRepository.get(it.accountId).getOrThrow()
        } ?: accountRepository.getCurrentAccount().getOrThrow()
    }
}