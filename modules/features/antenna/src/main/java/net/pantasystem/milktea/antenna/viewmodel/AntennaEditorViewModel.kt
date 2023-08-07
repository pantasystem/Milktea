package net.pantasystem.milktea.antenna.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.antenna.AntennaRepository
import net.pantasystem.milktea.model.antenna.AntennaSource
import net.pantasystem.milktea.model.antenna.SaveAntennaParam
import net.pantasystem.milktea.model.antenna.from
import net.pantasystem.milktea.model.antenna.str
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListRepository
import net.pantasystem.milktea.model.user.User
import java.util.regex.Pattern
import javax.inject.Inject



@HiltViewModel
class AntennaEditorViewModel @Inject constructor(
    private val userViewDataFactory: UserViewData.Factory,
    loggerFactory: Logger.Factory,
    private val accountRepository: AccountRepository,
    private val userListRepository: UserListRepository,
    private val antennaRepository: AntennaRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(){


    companion object {
        const val STATE_ANTENNA_ID = "antenna_id"
        const val STATE_LIST_ID = "list_id"
        const val STATE_GROUP_ID = "group_id"
        const val STATE_NAME = "name"
        const val STATE_SOURCE = "source"
        const val STATE_KEYWORDS = "keywords"
        const val STATE_EXCLUDE_KEYWORDS = "exclude_keywords"
        const val STATE_USERNAME_LIST = "username_list"
        const val STATE_NOTIFY = "notify"
        const val STATE_WITH_FILE = "with_files"
        const val CASE_SENSITIVE = "case_sensitive"
        const val STATE_WITH_REPLIES = "with_replies"

    }
    private val logger = loggerFactory.create("AntennaEditorViewModel")


    private val _antennaId = savedStateHandle.getStateFlow<Antenna.Id?>(STATE_ANTENNA_ID, null)


    val antenna = _antennaId.filterNotNull().map {
        antennaRepository.find(it).getOrNull()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    val name = savedStateHandle.getStateFlow<String?>(STATE_NAME, null)

    val source = savedStateHandle.getStateFlow<String>(STATE_SOURCE, AntennaSource.All.str()).map {
        AntennaSource.from(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AntennaSource.All,
    )

    val isList = source.map {
        it is AntennaSource.List
    }

    val isUsers = source.map {
        it is AntennaSource.Users
    }

    val isGroup = source.map {
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

    val userListId = savedStateHandle.getStateFlow<UserList.Id?>(STATE_LIST_ID, null)

    val userList = userListId.map {
        if (it == null) null else userListRepository.findOne(it)
    }.catch {
        emit(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

//    val groupList = MediatorLiveData<List<Group>?>().apply{
//        addSource(this@AntennaEditorViewModel.source){
//            /*if(it == Source.GROUP && this.value.isNullOrEmpty()){
//                miCore.getMisskeyAPI(accountRelation)
//            }*/
//        }
//    }
//
//    val group = MediatorLiveData<Group>().apply{
//        addSource(groupList){
//            this.value = it?.firstOrNull { g ->
//                g.id == this@AntennaEditorViewModel.antenna.value?.userGroupId
//            }?: it?.firstOrNull()
//        }
//    }

    /**
     * 新しいノートを通知します
     */
    val notify = savedStateHandle.getStateFlow(STATE_NOTIFY, true)

    /**
     * ファイルが添付されたノートのみ
     */
    val withFile =  savedStateHandle.getStateFlow(STATE_WITH_FILE, false)

    /**
     * 大文字と小文字を区別します
     */
    val caseSensitive =  savedStateHandle.getStateFlow(CASE_SENSITIVE, false)
    /**
     * 返信を含める
     */
    val withReplies = savedStateHandle.getStateFlow(STATE_WITH_REPLIES, false)

    val antennaAddedStateEvent = EventBus<Boolean>()
    
    fun createOrUpdate(){

        viewModelScope.launch {
            runCancellableCatching {
                val account = getAccount().getOrThrow()
                val antenna = savedStateHandle.get<Antenna.Id?>(STATE_ANTENNA_ID)?.let {
                    antennaRepository.find(it).getOrThrow()
                }
                val params = SaveAntennaParam(
                    savedStateHandle[STATE_NAME] ?: antenna?.name ?: "",
                    savedStateHandle.get<String?>(STATE_SOURCE)?.let {
                                AntennaSource.from(it)
                    } ?: antenna?.src ?: AntennaSource.All,
                    savedStateHandle[STATE_LIST_ID] ?: antenna?.userListId,
                    null,
                    toListKeywords(savedStateHandle[STATE_KEYWORDS]?: ""),
                    toListKeywords(savedStateHandle[STATE_EXCLUDE_KEYWORDS] ?: ""),
                    savedStateHandle[STATE_USERNAME_LIST] ?: antenna?.users ?: emptyList(),
                    savedStateHandle[CASE_SENSITIVE] ?: antenna?.caseSensitive ?: false,
                    savedStateHandle[STATE_WITH_FILE] ?: antenna?.withFile ?: false,
                    savedStateHandle[STATE_WITH_REPLIES] ?: antenna?.withReplies ?: false,
                    savedStateHandle[STATE_NOTIFY] ?: antenna?.notify ?: true,
                    )
                if (antenna == null) {
                    antennaRepository.create(account.accountId, params)
                        .getOrThrow()
                } else {
                    antennaRepository.update(antenna.id, params)
                        .getOrThrow()
                }
            }.onSuccess {
                setAntennaId(it.id)
                withContext(Dispatchers.Main) {
                    antennaAddedStateEvent.event = true
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
        viewModelScope.launch {
            antennaRepository.find(antennaId).onSuccess { antenna ->
                savedStateHandle[STATE_NAME] = antenna.name
                savedStateHandle[STATE_SOURCE] = antenna.src.str()
                savedStateHandle[STATE_KEYWORDS] = setupKeywords(antenna.keywords)
                savedStateHandle[STATE_EXCLUDE_KEYWORDS] = setupKeywords(antenna.excludeKeywords)
                savedStateHandle[STATE_USERNAME_LIST] = antenna.users

                savedStateHandle[STATE_NOTIFY] = antenna.notify
                savedStateHandle[STATE_WITH_FILE] = antenna.withFile
                savedStateHandle[CASE_SENSITIVE] = antenna.caseSensitive
                savedStateHandle[STATE_WITH_REPLIES] = antenna.withReplies

                savedStateHandle[STATE_ANTENNA_ID] = antenna.id
                savedStateHandle[STATE_LIST_ID] = antenna.userListId
                savedStateHandle[STATE_GROUP_ID] = antenna.userGroupId
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