package jp.panta.misskeyandroidclient.ui.users.viewmodel

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.notes.NoteTranslationStore
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase
import java.lang.IllegalArgumentException

class UserDetailViewModel @AssistedInject constructor(
    val miCore: MiCore,
    private val translationStore: net.pantasystem.milktea.model.notes.NoteTranslationStore,
    private val deleteNicknameUseCase: net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase,
    private val updateNicknameUseCase: net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase,
    val accountStore: net.pantasystem.milktea.model.account.AccountStore,
    @Assisted val userId: net.pantasystem.milktea.model.user.User.Id?,
    @Assisted private val fqdnUserName: String?,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(userId: net.pantasystem.milktea.model.user.User.Id?, fqdnUserName: String?): UserDetailViewModel
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
        it as? net.pantasystem.milktea.model.user.User.Detail
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


    val showFollowers = EventBus<net.pantasystem.milktea.model.user.User?>()
    val showFollows = EventBus<net.pantasystem.milktea.model.user.User?>()

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
                    val user = miCore.getUserRepository().find(it.id) as net.pantasystem.milktea.model.user.User.Detail
                    if (user.isFollowing || user.hasPendingFollowRequestFromYou) {
                        miCore.getUserRepository().unfollow(user.id)
                    } else {
                        miCore.getUserRepository().follow(user.id)
                    }
                    miCore.getUserRepository().find(user.id) as net.pantasystem.milktea.model.user.User.Detail
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
                    (miCore.getUserRepository().find(it.id, true) as net.pantasystem.milktea.model.user.User.Detail)
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

    private suspend fun findUser(): net.pantasystem.milktea.model.user.User {
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

    private var mAc: net.pantasystem.milktea.model.account.Account? = null
    private suspend fun getAccount(): net.pantasystem.milktea.model.account.Account {
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
    userId: net.pantasystem.milktea.model.user.User.Id
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