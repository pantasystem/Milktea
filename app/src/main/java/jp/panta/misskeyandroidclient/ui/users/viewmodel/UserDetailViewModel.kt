package jp.panta.misskeyandroidclient.ui.users.viewmodel

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountStore
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.nickname.DeleteNicknameUseCase
import jp.panta.misskeyandroidclient.model.users.nickname.UpdateNicknameUseCase
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.IllegalArgumentException

class UserDetailViewModel @AssistedInject constructor(
    val miCore: MiCore,
    private val translationStore: NoteTranslationStore,
    private val deleteNicknameUseCase: DeleteNicknameUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    val accountStore: AccountStore,
    @Assisted val userId: User.Id?,
    @Assisted private val fqdnUserName: String?,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(userId: User.Id?, fqdnUserName: String?): UserDetailViewModel
    }

    companion object;

    private val logger = miCore.loggerFactory.create("UserDetailViewModel")
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO


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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    val isMine = combine(userState, accountStore.state) { userState, accountState ->
        userState?.id?.id == accountState.currentAccount?.remoteId
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)


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
                updateNicknameUseCase(user, name)
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
                deleteNicknameUseCase(user)
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
        } ?: fqdnUserName?.let {
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

@Suppress("UNCHECKED_CAST")
fun UserDetailViewModel.Companion.provideFactory(
    assistedFactory: UserDetailViewModel.ViewModelAssistedFactory,
    userId: User.Id
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(userId, null) as T
    }
}

@Suppress("UNCHECKED_CAST")
fun UserDetailViewModel.Companion.provideFactory(
    assistedFactory: UserDetailViewModel.ViewModelAssistedFactory,
    fqdnUserName: String,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(null, fqdnUserName) as T
    }
}