package jp.panta.misskeyandroidclient.ui.users.viewmodel

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.nickname.DeleteNicknameUseCase
import jp.panta.misskeyandroidclient.model.users.nickname.UpdateNicknameUseCase
import jp.panta.misskeyandroidclient.model.users.nickname.UserNickname
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.IllegalArgumentException

@ExperimentalCoroutinesApi
class UserDetailViewModel(
    val userId: User.Id?,
    private val fqdnUserName: String?,
    val miCore: MiCore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val translationStore: NoteTranslationStore
) : ViewModel() {
    private val logger = miCore.loggerFactory.create("UserDetailViewModel")

    val userState = miCore.getUserDataSource().state.map { state ->
        when {
            userId != null -> {
                state.get(userId)
            }
            fqdnUserName != null -> {
                state.get(fqdnUserName)
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }.mapNotNull {
        logger.debug("flow user:$it")
        it as? User.Detail
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val user = userState.asLiveData()

    private val pinNotesState = userState.filterNotNull().map {
        it.pinnedNoteIds?.map { id ->
            miCore.getNoteDataSource().get(id)
        } ?: emptyList()
    }

    val profileUrl = userState.filterNotNull().map {
        val ac = miCore.getAccountRepository().get(it.id.accountId)
        it.getProfileUrl(ac)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pinNotes = MediatorLiveData<List<PlaneNoteViewData>>().apply {

        pinNotesState.map { notes ->
            notes.map { note ->
                PlaneNoteViewData(
                    miCore.getGetters().noteRelationGetter.get(note),
                    getAccount(),
                    miCore.getNoteCaptureAdapter(),
                    translationStore
                )
            }
        }.onEach {
            this.postValue(it)

        }.flatMapLatest {
            it.map { n ->
                n.eventFlow
            }.merge()
        }.catch { e ->
            logger.warning("", e = e)
        }.launchIn(viewModelScope + dispatcher)
    }

    val isMine = MutableLiveData<Boolean>()

    val isFollowing = MediatorLiveData<Boolean>().apply {
        addSource(user) {
            this.value = it?.isFollowing
        }
    }


    val userName = MediatorLiveData<String>().apply {
        addSource(user) { user ->
            user?.getDisplayUserName()
        }
    }

    val isBlocking = MediatorLiveData<Boolean>().apply {
        value = user.value?.isBlocking ?: false
        addSource(user) {
            value = it?.isBlocking
        }
    }

    val isMuted = MediatorLiveData<Boolean>().apply {
        value = user.value?.isMuting ?: false
        addSource(user) {
            value = it?.isMuting
        }
    }

    val isRemoteUser = MediatorLiveData<Boolean>().apply {
        addSource(user) {
            value = it?.url != null
        }
    }
    val showFollowers = EventBus<User?>()
    val showFollows = EventBus<User?>()


    init {
        require(userId != null || fqdnUserName != null) {
            "userIdかfqdnUserNameのいずれかが指定されている必要があります。"
        }
        load()
    }

    fun load() {
        viewModelScope.launch(dispatcher) {
            runCatching {
                findUser()
            }.onFailure {
                logger.error("読み込みエラー", e = it)
            }.onSuccess { u ->
                logger.debug("user:$u")
            }
        }
    }

    fun changeFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    val user = miCore.getUserRepository().find(it.id) as User.Detail
                    if (user.isFollowing || user.hasPendingFollowRequestFromYou) {
                        miCore.getUserRepository().unfollow(user.id)
                    } else {
                        miCore.getUserRepository().follow(user.id)
                    }
                    miCore.getUserRepository().find(user.id) as User.Detail
                }.onSuccess {

                }.onFailure {
                    logger.error("unmute", e = it)
                }
            }

        }
    }

    fun showFollows() {
        showFollows.event = user.value
    }

    fun showFollowers() {
        showFollowers.event = user.value
    }

    fun mute() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    miCore.getUserRepository().mute(it.id)
                }.onSuccess {

                }.onFailure {
                    logger.error("unmute", e = it)
                }
            }
        }
    }

    fun unmute() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    miCore.getUserRepository().unmute(it.id)
                }.onSuccess {

                }.onFailure {
                    logger.error("unmute", e = it)
                }
            }
        }
    }

    fun block() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    miCore.getUserRepository().block(it.id)
                }.onSuccess {

                }
            }
        }
    }

    fun unblock() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    miCore.getUserRepository().unblock(it.id)
                    (miCore.getUserRepository().find(it.id, true) as User.Detail)
                }.onSuccess {

                }
            }
        }
    }

    fun changeNickname(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val user = findUser()
                UpdateNicknameUseCase(
                    miCore.getAccountRepository(),
                    miCore.getUserDataSource(),
                    miCore.getUserNicknameRepository(),
                    user,
                    name
                ).execute()
            }.onSuccess {
                logger.debug("ニックネーム更新処理成功")
            }.onFailure {
                logger.error("ニックネーム更新処理失敗", e = it)
            }
        }
    }

    fun deleteNickname() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val user = findUser()
                DeleteNicknameUseCase(
                    miCore.getUserNicknameRepository(),
                    miCore.getAccountRepository(),
                    miCore.getUserDataSource(),
                    user
                ).execute()
            }.onSuccess {
                logger.debug("ニックネーム削除処理成功")
            }.onFailure {
                logger.error("ニックネーム削除失敗", e = it)
            }
        }
    }

    private suspend fun findUser(): User {
        val u = userId?.let {
            runCatching {
                miCore.getUserRepository().find(userId!!, true)
            }.onSuccess {
                logger.debug("ユーザー取得成功:$it")
            }.onFailure {
                logger.error("ユーザー取得失敗", e = it)
            }.getOrNull()
        }?: fqdnUserName?.let {
            val account = getAccount()
            val userNameAndHost = fqdnUserName.split("@").filter { it.isNotBlank() }
            val userName = userNameAndHost[0]
            val host = userNameAndHost.getOrNull(1)
            runCatching {
                miCore.getUserRepository().findByUserName(account.accountId, userName, host, true)
            }.onFailure {
                logger.error("ユーザー取得失敗", e = it)
            }.onSuccess {
                logger.debug("ユーザー取得成功: $it")
            }.getOrNull()
        }
        return u!!
    }

    private var mAc: Account? = null
    private suspend fun getAccount(): Account {
        if (mAc != null) {
            return mAc!!
        }
        if (userId != null) {
            mAc = miCore.getAccountRepository().get(userId.accountId)
            return mAc!!
        }

        mAc = miCore.getAccountRepository().getCurrentAccount()
        return mAc!!
    }

}