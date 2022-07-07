package jp.panta.misskeyandroidclient.ui.users.viewmodel

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteTranslationStore
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase

class UserDetailViewModel @AssistedInject constructor(
    private val translationStore: NoteTranslationStore,
    private val deleteNicknameUseCase: DeleteNicknameUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val noteDataSource: NoteDataSource,
    userDataSource: UserDataSource,
    loggerFactory: Logger.Factory,
    private val noteRelationGetter: NoteRelationGetter,
    private val userRepository: UserRepository,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    @Assisted val userId: User.Id?,
    @Assisted private val fqdnUserName: String?,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(userId: User.Id?, fqdnUserName: String?): UserDetailViewModel
    }

    companion object;

    private val logger = loggerFactory.create("UserDetailViewModel")
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO


    val userState = userDataSource.state.map { state ->
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
            noteDataSource.get(id)
        } ?: emptyList()
    }

    val profileUrl = userState.filterNotNull().map {
        val ac = accountRepository.get(it.id.accountId)
        it.getProfileUrl(ac)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pinNotes = MediatorLiveData<List<PlaneNoteViewData>>().apply {
        pinNotesState.map { notes ->
            notes.mapNotNull {
                noteRelationGetter.get(it).getOrNull()
            }.map { note ->
                PlaneNoteViewData(
                    note,
                    getAccount(),
                    noteCaptureAPIAdapter,
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
                    val user = userRepository.find(it.id) as User.Detail
                    if (user.isFollowing || user.hasPendingFollowRequestFromYou) {
                        userRepository.unfollow(user.id)
                    } else {
                        userRepository.follow(user.id)
                    }
                    userRepository.find(user.id) as User.Detail
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
                    userRepository.mute(it.id)
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
                    userRepository.unmute(it.id)
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
                    userRepository.block(it.id)
                }.onSuccess {

                }
            }
        }
    }

    fun unblock() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    userRepository.unblock(it.id)
                    (userRepository.find(it.id, true) as User.Detail)
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
                userRepository.find(userId!!, true)
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
                userRepository.findByUserName(account.accountId, userName, host, true)
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
            mAc = accountRepository.get(userId.accountId)
            return mAc!!
        }

        mAc = accountRepository.getCurrentAccount()
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